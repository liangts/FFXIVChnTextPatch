package link.teemo.sqpack;

import com.shenou.fs.core.utils.res.Config;
import link.teemo.sqpack.util.HexUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SourceMapTest {
    public static void main(String[] args) throws  Exception{
        File srcFile = new File("C:\\Users\\SOX\\Desktop\\4.2\\src.txt");
        FileReader srcFileReader = new FileReader(srcFile);
        BufferedReader srcBufferedReader=new BufferedReader(srcFileReader);
        HashMap<Integer,String> srcMap = new HashMap<>();
        String line = "";
        String[] arrs = null;
        while ((line=srcBufferedReader.readLine())!=null) {
            arrs = line.split("[|]");
            if(arrs.length == 2)
                srcMap.put(Integer.valueOf(arrs[0]), arrs[1]);
        }
        srcBufferedReader.close();
        srcFileReader.close();

        File dstFile = new File("C:\\Users\\SOX\\Desktop\\4.2\\dst.txt");
        FileReader dstFileReader = new FileReader(dstFile);
        BufferedReader dstBufferedReader=new BufferedReader(dstFileReader);
        HashMap<Integer,String> dstMap = new HashMap<>();
        while ((line=dstBufferedReader.readLine())!=null) {
            arrs = line.split("[|]");
            if(arrs.length == 2)
                dstMap.put(Integer.valueOf(arrs[0]), arrs[1]);
        }
        dstBufferedReader.close();
        dstFileReader.close();

        HashMap<String,String> pkMap = new HashMap<>();
        Iterator iter = srcMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Integer,String> entry = (Map.Entry) iter.next();
            Integer key = entry.getKey();
            String val = entry.getValue();
            if(pkMap.get(val)==null && dstMap.get(key)!=null && dstMap.get(key).length()>0){
                pkMap.put(val, dstMap.get(key));
            }
        }

        Config.setConfigResource("conf" + File.separator + "transtring.properties");
        System.out.println(Config.getProperty("被検世界「シグマ」V1.0"));
//        Config.setProperty(":","魔陶器：解咒");
//        Config.saveProperty();
//        FileOutputStream out = new FileOutputStream("conf" + File.separator + "pk.properties");
//        OutputStreamWriter outWriter = new OutputStreamWriter(out, "UTF-8");
//        BufferedWriter bufWrite = new BufferedWriter(outWriter);
//        iter = pkMap.entrySet().iterator();
//        while (iter.hasNext()) {
//            Map.Entry<String,String> entry = (Map.Entry) iter.next();
//            String key = entry.getKey();
//            String val = entry.getValue();
//            bufWrite.write(key.replace(":","\\:") + "=" + val);
//            bufWrite.newLine();
//        }
//        bufWrite.close();
//        outWriter.close();
//        out.close();
//
        File file = new File("C:\\Users\\SOX\\Desktop\\4.2\\instancedatatest.txt");
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader=new BufferedReader(fileReader);
        while ((line=bufferedReader.readLine())!=null) {
            arrs = line.split("[|]");
            if(arrs.length>1)
            System.out.println("exd/instancecontenttextdata_" + arrs[0] + "_1=" + HexUtils.bytesToHexStringWithOutSpace(arrs[1].getBytes("UTF-8")));
        }
        srcBufferedReader.close();
        srcFileReader.close();

    }
}
