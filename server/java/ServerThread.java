import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Timer;
import java.util.TimerTask;
import javax.microedition.io.StreamConnection;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.HashDocAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.standard.Chromaticity;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.attribute.standard.PageRanges;
import javax.print.attribute.standard.PrintQuality;
import javax.print.attribute.standard.Sides;

import net.sf.json.JSONObject;

public class ServerThread extends Thread {
	private StreamConnection streamConnection;
	private InputStream inputStream;
	private InputStreamReader inputStreamReader;
	private OutputStream outputStream;
	private OutputStreamWriter outputStreamWriter;
	private JSONObject jsonin;
	private JSONObject jsonout;
	private File myFile;
	
	private String mediaSize;
	private int copies;
	private String orientation;
	private String chromaticity;
	private String sides;
	private String quality;
	private String pageRange;
	public ServerThread(StreamConnection sc) {
		streamConnection=sc;
	}
	@Override
	public void run() {
		mediaSize="";
		copies=1;
		orientation="";
		chromaticity="";
		sides="";
		quality="";
		pageRange="";
		char[] chars=new char[1024];
		try {
			inputStream=streamConnection.openInputStream();
			outputStream=streamConnection.openOutputStream();
			inputStreamReader=new InputStreamReader(inputStream);
			inputStreamReader.read(chars);
			jsonout=new JSONObject();
			jsonout.put("result", "success");
			outputStreamWriter=new OutputStreamWriter(outputStream);
			outputStreamWriter.write(jsonout.toString());
			outputStreamWriter.flush();
		}catch(Exception e) {
			e.printStackTrace();
		}
		try {
			System.out.println(String.valueOf(chars));
			jsonin=JSONObject.fromObject(String.valueOf(chars));
			myFile = new File(jsonin.getString("fileName"));
			FileOutputStream fileOutputStream=new FileOutputStream(myFile);
			byte[] buffer=new byte[1024*100];
			int tmp;
			while((tmp=inputStream.read(buffer,0,buffer.length))>0) {
				fileOutputStream.write(buffer,0,tmp);
				fileOutputStream.flush();
			}
			fileOutputStream.close();
			streamConnection.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
		mediaSize = jsonin.getString("mediaSize");
		//copies = Integer.parseInt(jsonin.getString("copies"));
		pageRange = jsonin.getString("pageRange");
		orientation = jsonin.getString("orientation");
		chromaticity = jsonin.getString("chromaticity");
		//sides = jsonin.getString("sides");
		//quality = jsonin.getString("quality");
		//构建打印请求属性集   
		HashPrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
		switch(mediaSize) {
			case "MediaSize.ISO.A4":pras.add(MediaSize.ISO.A4);break;
			case "MediaSize.ISO.A5":pras.add(MediaSize.ISO.A5);break;
			case "MediaSize.ISO.B5":pras.add(MediaSize.ISO.B5);break;
			case "MediaSize.ISO.C5":pras.add(MediaSize.ISO.C5);break;
			case "MediaSize.ISO.C6":pras.add(MediaSize.ISO.C6);break;
			case "MediaSize.ISO.DESIGNATED_LONG":pras.add(MediaSize.ISO.DESIGNATED_LONG);break;
			default:break;
		}
		//pras.add(new Copies(copies));
		switch(pageRange) {
			case "整个文档": break;
			case "自定义":pras.add(new PageRanges(jsonin.getInt("firstPage"),jsonin.getInt("lastPage")));break;
		}
		
		switch(orientation) {
			case "PORTRAIT":pras.add(OrientationRequested.PORTRAIT);break;
			case "LANDSCAPE":pras.add(OrientationRequested.LANDSCAPE);break;
			default:break;
		}
		switch(chromaticity) {
			case "COLOR":pras.add(Chromaticity.COLOR);break;
			case "MONOCHROME":pras.add(Chromaticity.MONOCHROME);break;
			default : break;
		}
		switch(sides){
			case"DUPLEX":pras.add(Sides.DUPLEX);break;
			case"ONE_SIDED":break;
			default:break;
		}
		switch(quality) {
			case "DRAFT":pras.add(PrintQuality.DRAFT);break;
			case "HIGH":pras.add(PrintQuality.HIGH);break;
			case "NORMAL":pras.add(PrintQuality.NORMAL);break;
			default:break;
		}
		//设置打印格式，因为未确定类型，所以选择autosense   
		DocFlavor flavor = DocFlavor.INPUT_STREAM.PDF; 
		//查找所有的可用的打印服务   
		PrintService printService[] = PrintServiceLookup.lookupPrintServices(flavor, pras);   
		//定位默认的打印服务   
		PrintService defaultService = PrintServiceLookup.lookupDefaultPrintService();
		//显示打印对话框   
		//PrintService service = ServiceUI.printDialog(null, 200, 200, printService,    
		//        defaultService, flavor, pras);
		//System.out.println(String.valueOf(buffer));
		if(printService.length>0){
      		try {  
      			System.out.println("start print");
      			FileInputStream fileInputStream = new FileInputStream(myFile);	
				DocPrintJob job = printService[0].createPrintJob(); //创建打印作业   
				DocAttributeSet das = new HashDocAttributeSet();   
				Doc doc = new SimpleDoc(fileInputStream, flavor, das); 
				System.out.println("2 printing");  
				job.print(doc, pras);
				System.out.println("2 printed");
			} catch (Exception e) {   
				System.out.println("Error!");
				e.printStackTrace();   
			}
		}else if (defaultService!=null){
			try {  
      			System.out.println("start print");
      			FileInputStream fileInputStream = new FileInputStream(myFile);	
				DocPrintJob job = defaultService.createPrintJob(); //创建打印作业   
				DocAttributeSet das = new HashDocAttributeSet();   
				Doc doc = new SimpleDoc(fileInputStream, flavor, das); 
				System.out.println("defaultService printing");  
				job.print(doc, null);  
				System.out.println("defaultService printed");
			} catch (Exception e) {   
				System.out.println("Error!");
				e.printStackTrace();   
			}
		}else {
			System.out.println("no printer");
		}
	}
}
