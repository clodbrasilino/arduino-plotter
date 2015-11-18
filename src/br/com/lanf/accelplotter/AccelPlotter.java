package br.com.lanf.accelplotter;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RefineryUtilities;

import purejavacomm.CommPortIdentifier;
import purejavacomm.NoSuchPortException;
import purejavacomm.SerialPort;

public class AccelPlotter {

	@SuppressWarnings("unused")
	public static void main(String[] args) throws Exception {
		String m_TestPortName = args[0];
		try {
			CommPortIdentifier portid = CommPortIdentifier.getPortIdentifier(m_TestPortName);
			SerialPort m_Port = (SerialPort) portid.open("teste", 1000);
			OutputStream m_Out = m_Port.getOutputStream();
			InputStream m_In = m_Port.getInputStream();
			drain(m_In);
			XYSeries xSeries = new XYSeries("X");
			XYSeries ySeries = new XYSeries("Y");
			XYSeries xAvgSeries = new XYSeries("Avg X");
			XYSeries yAvgSeries = new XYSeries("Avg Y");
			AccelPlot grafico = null;
			while(true){
				if(m_In.available()>0){
					Thread.sleep(300);
					byte[] b = new byte[1000];
					int bytesLidos = 0;
					bytesLidos = m_In.read(b);
					String rawData = new String(b, "ASCII");
					String[] lines = rawData.split("\n");
					Float tempo = 0f;
					for(int i = 0; i < lines.length; ++i){
						try {
							String[] data = lines[i].split("\t");
							Float xData = new Float(data[3]);
							Float yData = new Float(data[4]);
							Float timeData = new Float(data[2]);
							tempo = timeData;
							xSeries.add(timeData, xData);
							ySeries.add(timeData, yData);
							System.out.println(xData + " "+ yData+ " - "+timeData);
						} catch (Exception e) {
							System.err.println("deu pau pra colocar os dados na serie!");
						}
					}
					
					XYSeriesCollection dados = new XYSeriesCollection();
					//dados.addSeries(xSeries);
					//dados.addSeries(ySeries);
					@SuppressWarnings("rawtypes")
					List dadosX = xSeries.getItems();
					@SuppressWarnings("rawtypes")
					List dadosY = ySeries.getItems();
					double acumuladorX = 0, acumuladorY = 0;
					try{
						for (int i = dadosX.size()-1; i > dadosX.size()-11; --i) {
							acumuladorX +=  ((XYDataItem)dadosX.get(i)).getYValue();
						}
						for (int i = dadosY.size()-1; i > dadosY.size()-11; --i) {
							acumuladorY +=  ((XYDataItem)dadosY.get(i)).getYValue();
						}
					}catch(Exception ex){
						
					}
					acumuladorX /= 10;
					acumuladorY /= 10;
					if (tempo > 0) xAvgSeries.add(new XYDataItem((double) tempo,(double) acumuladorX));
					if (tempo > 0) yAvgSeries.add(new XYDataItem((double) tempo,(double) acumuladorY));
					dados.addSeries(xAvgSeries);
					dados.addSeries(yAvgSeries);
					if (grafico == null) grafico = new AccelPlot("Acelerômetro", "Tempo", "Aceleração", dados);
			        grafico.pack();
			        RefineryUtilities.centerFrameOnScreen(grafico);
			        grafico.setVisible(true);
				}
			}
		} catch (NoSuchPortException e) {
			fail("could no open port '%s'\n", m_TestPortName);
		}
	}
	
	static protected void drain(InputStream ins) throws Exception {
		Thread.sleep(100);
		int n;
		while ((n = ins.available()) > 0) {
			for (int i = 0; i < n; ++i)
				ins.read();
			Thread.sleep(100);
		}
	}
	
	static void fail(String format, Object... args) throws TestFailedException {
		System.out.println(" FAILED");
		System.out.println("------------------------------------------------------------");
		System.out.printf(format, args);
		System.out.println();
		System.out.println("------------------------------------------------------------");
		throw new TestFailedException();

	}
	
	static class TestFailedException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = -2638870083246738946L;

	}
}
