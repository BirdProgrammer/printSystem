package com.ct.ti;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.HashAttributeSet;
import javax.print.attribute.HashDocAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.standard.Chromaticity;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.attribute.standard.PageRanges;
import javax.print.attribute.standard.PrintQuality;
import javax.print.attribute.standard.Sides;
import javax.microedition.io.StreamConnection;

import net.sf.json.JSONObject;

public class ServerThread extends Thread {

    private StreamConnection streamConnection;
    private InputStream inputStream;
    private InputStreamReader inputStreamReader;
    private OutputStream outputStream;
    private OutputStreamWriter outputStreamWriter;
    private JSONObject jsonin;

    private File file;
    private File decodedFile;
    private FileInputStream fileInputStream;

    private String mediaSize;
    private int copies;
    private String orientation;
    private String chromaticity;
    private String sides;
    private String quality;
    private String pageRange;
    private boolean lock;
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
            outputStreamWriter=new OutputStreamWriter(outputStream);
            inputStreamReader.read(chars);
            jsonin=JSONObject.fromObject(String.valueOf(chars));
        }catch(Exception e) {
            e.printStackTrace();
        }

        System.out.println(String.valueOf(chars));

        mediaSize = jsonin.getString("mediaSize");
        copies = Integer.parseInt(jsonin.getString("copies"));

        pageRange = jsonin.getString("pageRange");
        orientation = jsonin.getString("orientation");
        chromaticity = jsonin.getString("chromaticity");
        sides = jsonin.getString("sides");
        quality = jsonin.getString("quality");
        lock = jsonin.getBoolean("lock");
        //构建打印请求属性集
        HashPrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
        DocAttributeSet das = new HashDocAttributeSet();
        HashAttributeSet has = new HashAttributeSet();
        switch(mediaSize) {
            case "MediaSize.ISO.A4":pras.add(MediaSize.ISO.A4);break;
            // case "MediaSize.ISO.A5":pras.add(MediaSize.ISO.A5);break;
            // case "MediaSize.ISO.B5":pras.add(MediaSize.ISO.B5);break;
            // case "MediaSize.ISO.C5":pras.add(MediaSize.ISO.C5);break;
            // case "MediaSize.ISO.C6":pras.add(MediaSize.ISO.C6);break;
            // case "MediaSize.ISO.DESIGNATED_LONG":pras.add(MediaSize.ISO.DESIGNATED_LONG);break;
            default:has.add(MediaSize.ISO.A4);break;
        }

        pras.add(new Copies(copies));
        switch(pageRange) {
            case "ALL": break;
            case "SPECIFIC":has.add(new PageRanges(jsonin.getInt("firstPage"),jsonin.getInt("lastPage")));
                            pras.add(new PageRanges(jsonin.getInt("firstPage"),jsonin.getInt("lastPage")));
                            break;
        }

        switch(orientation) {
            case "PORTRAIT":pras.add(OrientationRequested.PORTRAIT);break;
            case "LANDSCAPE":pras.add(OrientationRequested.LANDSCAPE);break;
            default:break;
        }
        switch(chromaticity) {
            case "COLOR":pras.add(Chromaticity.COLOR);break;
            case "MONOCHROME":has.add(Chromaticity.MONOCHROME);pras.add(Chromaticity.MONOCHROME);break;
            default : break;
        }

        switch(sides){
            case"DUPLEX":has.add(Sides.DUPLEX);pras.add(Sides.DUPLEX);break;
            case"ONE_SIDED":pras.add(Sides.ONE_SIDED);break;
            default:break;
        }
        switch(quality) {
            case "DRAFT":pras.add(PrintQuality.DRAFT);break;
            case "HIGH":pras.add(PrintQuality.HIGH);break;
            case "NORMAL":pras.add(PrintQuality.NORMAL);break;
            default:break;
        }

		//设置打印格式，因为未确定类型，所以选择autosense
        DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;

        PrintService printService[] = PrintServiceLookup.lookupPrintServices(flavor, has);
        if(printService.length > 0){
            try {

                JSONObject jsonout=new JSONObject();
                jsonout.put("result", "success");
                outputStreamWriter.write(jsonout.toString());
                outputStreamWriter.flush();

                file = new File(jsonin.getString("fileName"));
                FileOutputStream fileOutputStream=new FileOutputStream(file);
                byte[] buffer=new byte[1024];
                int tmp;
                while((tmp=inputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer,0,tmp);
                    fileOutputStream.flush(); 
                }
                
                fileOutputStream.close();

                if(lock){
                    decodedFile = new File("decoded_"+file.getName());
                    decode.decryptFile(file,decodedFile);
                    fileInputStream = new FileInputStream(file);
                }else{
                    fileInputStream = new FileInputStream(file);
                }

                System.out.println("start printing");
                //创建打印作业
                DocPrintJob job = printService[0].createPrintJob();
                Doc doc = new SimpleDoc(fileInputStream, flavor, das);
                System.out.println("printing");
                job.print(doc, pras);
                System.out.println("printed");
            } catch (Exception e) {
                System.out.println("Error!");
                e.printStackTrace();
            }finally{
				try{
					streamConnection.close();
				}catch(IOException e){
					e.printStackTrace();
				}
				
			}
        }else{
            System.out.println("no printer");
            try{
                JSONObject jsonout=new JSONObject();
                jsonout.put("result", "failure");
                outputStreamWriter.write(jsonout.toString());
                outputStreamWriter.flush();
            }catch(IOException e){
                e.printStackTrace();
            }
		}
		try{
			streamConnection.close();
			Thread.sleep(5000);
			file.delete();
			if(decodedFile != null){
			    decodedFile.delete();
            }
			System.out.println("closed");
		}catch(Exception e){
			e.printStackTrace();
		}

    }

}