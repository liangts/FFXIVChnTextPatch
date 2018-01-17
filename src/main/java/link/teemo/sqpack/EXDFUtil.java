package link.teemo.sqpack;

import com.shenou.fs.core.utils.res.Config;
import link.teemo.sqpack.entity.EXDStringLocate;
import link.teemo.sqpack.model.*;
import link.teemo.sqpack.util.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;


public class EXDFUtil {

    private String pathToIndexSE;
    private String pathToIndexCN;
    private List<String> fileList;

    public EXDFUtil(String pathToIndexSE, List<String> fileList){
        this.pathToIndexSE = pathToIndexSE;
        this.fileList = fileList;
    }

    public EXDFUtil(String pathToIndexSE, String pathToIndexCN, List<String> fileList){
        this.pathToIndexSE = pathToIndexSE;
        this.pathToIndexCN = pathToIndexCN;
        this.fileList = fileList;
    }

    public List<EXDStringLocate> search(String strTransOption, String strSearchInsert) throws Exception {
        List<EXDStringLocate> locates = new ArrayList<>();
        if( pathToIndexSE == null ){ return locates;}
        String patternStr = "^[a-zA-Z0-9_]*$";
        Pattern pattern = Pattern.compile(patternStr);
        HashMap<Integer, SqPackIndexFolder> indexSE = new SqPackIndex(pathToIndexSE).resloveIndex();
        // 根据传入的文件进行遍历
        int fileCount = 0;
        for (String replaceFile : fileList) {
            // 准备好文件目录名和文件名
            String filePatch = replaceFile.substring(0, replaceFile.lastIndexOf("/"));
            String fileName = replaceFile.substring(replaceFile.lastIndexOf("/") + 1) + ".EXH";
            // 计算文件目录CRC
            Integer filePatchCRC = FFCRC.ComputeCRC(filePatch.toLowerCase().getBytes());
            // 计算头文件CRC
            Integer exhFileCRC = FFCRC.ComputeCRC(fileName.toLowerCase().getBytes());
            // 解压并且解析头文件
            if (indexSE.get(filePatchCRC) == null) continue;
            SqPackIndexFile exhIndexFileSE = indexSE.get(filePatchCRC).getFiles().get(exhFileCRC);
            if (exhIndexFileSE == null) continue;
            byte[] exhFileSE = extractFile(pathToIndexSE, exhIndexFileSE.getOffset());
            EXHFFile exhSE = new EXHFFile(exhFileSE);
            if (exhSE.getLangs().length > 0) {
                // 根据头文件 轮询资源文件
                for (EXDFPage exdfPage : exhSE.getPages()) {
                    // 获取资源文件的CRC
                    Integer exdFileCRCJA = FFCRC.ComputeCRC((fileName.replace(".EXH", "_" + String.valueOf(exdfPage.pageNum) + "_JA.EXD")).toLowerCase().getBytes());
                    // 提取对应的文本文件
                    SqPackIndexFile exdIndexFileJA = indexSE.get(filePatchCRC).getFiles().get(exdFileCRCJA);
                    byte[] exdFileJA = null;
                    try {
                        exdFileJA = extractFile(pathToIndexSE, exdIndexFileJA.getOffset());
                    } catch (Exception jaEXDFileException) {
                        continue;
                    }
                    // 解压本文文件 提取内容
                    EXDFFile ja_exd = new EXDFFile(exdFileJA);
                    HashMap<Integer, byte[]> jaExdList = ja_exd.getEntrys();
                    for (Map.Entry<Integer, byte[]> listEntry : jaExdList.entrySet()) {
                        Integer listEntryIndex = listEntry.getKey();
                        EXDFEntry exdfEntryJA = new EXDFEntry(listEntry.getValue(), exhSE.getDatasetChunkSize());
                        int stringCount = 1;
                        for (EXDFDataset exdfDatasetSE : exhSE.getDatasets()) {
                            // 只限文本内容
                            if (exdfDatasetSE.type == 0) {
                                byte[] jaBytes = exdfEntryJA.getString(exdfDatasetSE.offset);
                                String jaStr = new String(jaBytes, "UTF-8");
                                if ((pattern.matcher(jaStr).find() && jaStr.contains("_"))
                                        || (jaBytes.length > 4 && jaBytes[0] == 0x02 && (jaBytes[1] == 0x28 || jaBytes[1] == 0x40) && ((int) jaBytes[2] + 3 == jaBytes.length))) {
                                } else {
                                    switch(strTransOption){
                                        case "String":
                                            if(FFXIVString.parseFFXIVString(jaBytes).toLowerCase().contains(strSearchInsert.toLowerCase())){
                                                EXDStringLocate locate = new EXDStringLocate();
                                                locate.setFileName(replaceFile);
                                                locate.setIndex(listEntryIndex);
                                                locate.setStrCount(stringCount);
                                                locate.setStrBody(jaBytes);
                                                locates.add(locate);
                                            }
                                            break;
                                        case "HexString":
                                            if(HexUtils.bytesToHexStringWithOutSpace(jaBytes).contains(strSearchInsert)){
                                                EXDStringLocate locate = new EXDStringLocate();
                                                locate.setFileName(replaceFile);
                                                locate.setIndex(listEntryIndex);
                                                locate.setStrCount(stringCount);
                                                locate.setStrBody(jaBytes);
                                                locates.add(locate);
                                            }
                                            break;
                                        default:
                                    }
                                }
                                stringCount++;
                            }
                        }
                    }
                }
            }
        }
        return locates;
    }
    public List<String> explorer(String fileExplorerOption, String fileExplorerFileName) throws Exception {
        List<String> locates = new ArrayList<>();
        if( pathToIndexSE == null )return locates;
        HashMap<Integer, SqPackIndexFolder> indexSE = new SqPackIndex(pathToIndexSE).resloveIndex();
        // 根据传入的文件进行遍历
        int fileCount = 0;
        String replaceFile = fileExplorerFileName;
        // 准备好文件目录名和文件名
        String filePatch = replaceFile.substring(0, replaceFile.lastIndexOf("/"));
        String fileName = replaceFile.substring(replaceFile.lastIndexOf("/") + 1) + ".EXH";
        // 计算文件目录CRC
        Integer filePatchCRC = FFCRC.ComputeCRC(filePatch.toLowerCase().getBytes());
        // 计算头文件CRC
        Integer exhFileCRC = FFCRC.ComputeCRC(fileName.toLowerCase().getBytes());
        // 解压并且解析头文件
        if (indexSE.get(filePatchCRC) == null) return locates;
        SqPackIndexFile exhIndexFileSE = indexSE.get(filePatchCRC).getFiles().get(exhFileCRC);
        if (exhIndexFileSE == null) return locates;
        byte[] exhFileSE = extractFile(pathToIndexSE, exhIndexFileSE.getOffset());
        EXHFFile exhSE = new EXHFFile(exhFileSE);
        if (exhSE.getLangs().length > 0) {
            // 根据头文件 轮询资源文件
            for (EXDFPage exdfPage : exhSE.getPages()) {
                // 获取资源文件的CRC
                Integer exdFileCRCJA = FFCRC.ComputeCRC((fileName.replace(".EXH", "_" + String.valueOf(exdfPage.pageNum) + "_JA.EXD")).toLowerCase().getBytes());
                // 提取对应的文本文件
                SqPackIndexFile exdIndexFileJA = indexSE.get(filePatchCRC).getFiles().get(exdFileCRCJA);
                byte[] exdFileJA = null;
                try {
                    exdFileJA = extractFile(pathToIndexSE, exdIndexFileJA.getOffset());
                } catch (Exception jaEXDFileException) {
                    continue;
                }
                // 解压本文文件 提取内容
                EXDFFile ja_exd = new EXDFFile(exdFileJA);
                HashMap<Integer, byte[]> jaExdList = ja_exd.getEntrys();
                for (Map.Entry<Integer, byte[]> listEntry : jaExdList.entrySet()) {
                    Integer listEntryIndex = listEntry.getKey();
                    EXDFEntry exdfEntryJA = new EXDFEntry(listEntry.getValue(), exhSE.getDatasetChunkSize());
                    String locate = listEntryIndex + "|";
                    for (EXDFDataset exdfDatasetSE : exhSE.getDatasets()) {
                        // 只限文本内容
                        if (exdfDatasetSE.type == 0) {
                            byte[] jaBytes = exdfEntryJA.getString(exdfDatasetSE.offset);
                            switch (fileExplorerOption) {
                                case "String":
                                    locate = locate + new String(jaBytes, "UTF-8") + "|";
                                    break;
                                case "FFXIVString":
                                    locate = locate + FFXIVString.parseFFXIVString(jaBytes) + "|";
                                    break;
                                case "HexString":
                                    locate = locate + HexUtils.bytesToHexStringWithOutSpace(jaBytes) + "|";
                                    break;
                                default:
                            }
                        }
                    }
                    locate = locate.substring(0, locate.length() -1 );
                    locates.add(locate);
                }
            }
        }
        return locates;
    }
    private HashMap<Integer, byte[]> exCompleteJournalCN() throws Exception{
        HashMap<Integer, byte[]> competeJournalMap = new HashMap<>();
        if( pathToIndexCN == null )return competeJournalMap;
        HashMap<Integer, SqPackIndexFolder> indexSE = new SqPackIndex(pathToIndexCN).resloveIndex();
        // 根据传入的文件进行遍历
        int fileCount = 0;
        String replaceFile = "EXD/CompleteJournal";
        // 准备好文件目录名和文件名
        String filePatch = replaceFile.substring(0, replaceFile.lastIndexOf("/"));
        String fileName = replaceFile.substring(replaceFile.lastIndexOf("/") + 1) + ".EXH";
        // 计算文件目录CRC
        Integer filePatchCRC = FFCRC.ComputeCRC(filePatch.toLowerCase().getBytes());
        // 计算头文件CRC
        Integer exhFileCRC = FFCRC.ComputeCRC(fileName.toLowerCase().getBytes());
        // 解压并且解析头文件
        if (indexSE.get(filePatchCRC) == null) return competeJournalMap;
        SqPackIndexFile exhIndexFileSE = indexSE.get(filePatchCRC).getFiles().get(exhFileCRC);
        if (exhIndexFileSE == null) return competeJournalMap;
        byte[] exhFileSE = extractFile(pathToIndexCN, exhIndexFileSE.getOffset());
        EXHFFile exhSE = new EXHFFile(exhFileSE);
        if (exhSE.getLangs().length > 0) {
            // 根据头文件 轮询资源文件
            for (EXDFPage exdfPage : exhSE.getPages()) {
                // 获取资源文件的CRC
                Integer exdFileCRCJA = FFCRC.ComputeCRC((fileName.replace(".EXH", "_" + String.valueOf(exdfPage.pageNum) + "_CHS.EXD")).toLowerCase().getBytes());
                // 提取对应的文本文件
                SqPackIndexFile exdIndexFileJA = indexSE.get(filePatchCRC).getFiles().get(exdFileCRCJA);
                byte[] exdFileJA = null;
                try {
                    exdFileJA = extractFile(pathToIndexCN, exdIndexFileJA.getOffset());
                } catch (Exception jaEXDFileException) {
                    continue;
                }
                // 解压本文文件 提取内容
                EXDFFile ja_exd = new EXDFFile(exdFileJA);
                HashMap<Integer, byte[]> jaExdList = ja_exd.getEntrys();
                for (Map.Entry<Integer, byte[]> listEntry : jaExdList.entrySet()) {
                    EXDFEntry exdfEntryJA = new EXDFEntry(listEntry.getValue(), exhSE.getDatasetChunkSize());
                    Integer key = null;
                    byte[] value = null;
                    for (EXDFDataset exdfDatasetSE : exhSE.getDatasets()) {
                        // 只限文本内容
                        if (exdfDatasetSE.type == 0x7 && exdfDatasetSE.offset == 0x4) {
                            key = exdfEntryJA.getInt(exdfDatasetSE.offset);
                        }
                        if (exdfDatasetSE.type == 0x0 && exdfDatasetSE.offset == 0x0) {
                            value = exdfEntryJA.getString(exdfDatasetSE.offset);
                        }
                        if(key != null && value != null && value.length > 0){
                            competeJournalMap.put(key, value);
                            break;
                        }
                    }
                }
            }
        }
        return competeJournalMap;
    }

    public HashMap<String, byte[]> exCompleteJournalSE(HashMap<String, byte[]> exMap) throws Exception{
        HashMap<Integer, byte[]> sourceMap = exCompleteJournalCN();
        if( pathToIndexSE == null )return exMap;
        HashMap<Integer, SqPackIndexFolder> indexSE = new SqPackIndex(pathToIndexSE).resloveIndex();
        // 根据传入的文件进行遍历
        int fileCount = 0;
        String replaceFile = "EXD/CompleteJournal";
        // 准备好文件目录名和文件名
        String filePatch = replaceFile.substring(0, replaceFile.lastIndexOf("/"));
        String fileName = replaceFile.substring(replaceFile.lastIndexOf("/") + 1) + ".EXH";
        // 计算文件目录CRC
        Integer filePatchCRC = FFCRC.ComputeCRC(filePatch.toLowerCase().getBytes());
        // 计算头文件CRC
        Integer exhFileCRC = FFCRC.ComputeCRC(fileName.toLowerCase().getBytes());
        // 解压并且解析头文件
        if (indexSE.get(filePatchCRC) == null) return exMap;
        SqPackIndexFile exhIndexFileSE = indexSE.get(filePatchCRC).getFiles().get(exhFileCRC);
        if (exhIndexFileSE == null) return exMap;
        byte[] exhFileSE = extractFile(pathToIndexSE, exhIndexFileSE.getOffset());
        EXHFFile exhSE = new EXHFFile(exhFileSE);
        if (exhSE.getLangs().length > 0) {
            // 根据头文件 轮询资源文件
            for (EXDFPage exdfPage : exhSE.getPages()) {
                // 获取资源文件的CRC
                Integer exdFileCRCJA = FFCRC.ComputeCRC((fileName.replace(".EXH", "_" + String.valueOf(exdfPage.pageNum) + "_JA.EXD")).toLowerCase().getBytes());
                // 提取对应的文本文件
                SqPackIndexFile exdIndexFileJA = indexSE.get(filePatchCRC).getFiles().get(exdFileCRCJA);
                byte[] exdFileJA = null;
                try {
                    exdFileJA = extractFile(pathToIndexSE, exdIndexFileJA.getOffset());
                } catch (Exception jaEXDFileException) {
                    continue;
                }
                // 解压本文文件 提取内容
                EXDFFile ja_exd = new EXDFFile(exdFileJA);
                HashMap<Integer, byte[]> jaExdList = ja_exd.getEntrys();
                for (Map.Entry<Integer, byte[]> listEntry : jaExdList.entrySet()) {
                    Integer listEntryIndex = listEntry.getKey();
                    EXDFEntry exdfEntryJA = new EXDFEntry(listEntry.getValue(), exhSE.getDatasetChunkSize());
                    for (EXDFDataset exdfDatasetSE : exhSE.getDatasets()) {
                        // 只限文本内容
                        if (exdfDatasetSE.type == 0x7 && exdfDatasetSE.offset == 0x4) {
                            Integer key = exdfEntryJA.getInt(exdfDatasetSE.offset);
                            if(sourceMap.get(key) != null){
                                exMap.put("CompleteJournal_".toLowerCase() + String.valueOf(listEntryIndex) + "_1",sourceMap.get(key));
                            }
                            break;
                        }
                    }
                }
            }
        }
        return exMap;
    }

    private HashMap<String, byte[]> exQuestCN() throws Exception{
        HashMap<String, byte[]> questMap = new HashMap<>();
        if( pathToIndexCN == null )return questMap;
        HashMap<Integer, SqPackIndexFolder> indexSE = new SqPackIndex(pathToIndexCN).resloveIndex();
        // 根据传入的文件进行遍历
        int fileCount = 0;
        String replaceFile = "EXD/Quest";
        // 准备好文件目录名和文件名
        String filePatch = replaceFile.substring(0, replaceFile.lastIndexOf("/"));
        String fileName = replaceFile.substring(replaceFile.lastIndexOf("/") + 1) + ".EXH";
        // 计算文件目录CRC
        Integer filePatchCRC = FFCRC.ComputeCRC(filePatch.toLowerCase().getBytes());
        // 计算头文件CRC
        Integer exhFileCRC = FFCRC.ComputeCRC(fileName.toLowerCase().getBytes());
        // 解压并且解析头文件
        if (indexSE.get(filePatchCRC) == null) return questMap;
        SqPackIndexFile exhIndexFileSE = indexSE.get(filePatchCRC).getFiles().get(exhFileCRC);
        if (exhIndexFileSE == null) return questMap;
        byte[] exhFileSE = extractFile(pathToIndexCN, exhIndexFileSE.getOffset());
        EXHFFile exhSE = new EXHFFile(exhFileSE);
        if (exhSE.getLangs().length > 0) {
            // 根据头文件 轮询资源文件
            for (EXDFPage exdfPage : exhSE.getPages()) {
                // 获取资源文件的CRC
                Integer exdFileCRCJA = FFCRC.ComputeCRC((fileName.replace(".EXH", "_" + String.valueOf(exdfPage.pageNum) + "_CHS.EXD")).toLowerCase().getBytes());
                // 提取对应的文本文件
                SqPackIndexFile exdIndexFileJA = indexSE.get(filePatchCRC).getFiles().get(exdFileCRCJA);
                byte[] exdFileJA = null;
                try {
                    exdFileJA = extractFile(pathToIndexCN, exdIndexFileJA.getOffset());
                } catch (Exception jaEXDFileException) {
                    continue;
                }
                // 解压本文文件 提取内容
                EXDFFile ja_exd = new EXDFFile(exdFileJA);
                HashMap<Integer, byte[]> jaExdList = ja_exd.getEntrys();
                for (Map.Entry<Integer, byte[]> listEntry : jaExdList.entrySet()) {
                    EXDFEntry exdfEntryJA = new EXDFEntry(listEntry.getValue(), exhSE.getDatasetChunkSize());
                    String key = null;
                    byte[] value = null;
                    for (EXDFDataset exdfDatasetSE : exhSE.getDatasets()) {
                        // 只限文本内容
                        if (exdfDatasetSE.type == 0x0 && exdfDatasetSE.offset == 0x0) {
                            value = exdfEntryJA.getString(exdfDatasetSE.offset);
                        }
                        if (exdfDatasetSE.type == 0x0 && exdfDatasetSE.offset == 0x968) {
                            key = new String(exdfEntryJA.getString(exdfDatasetSE.offset),"UTF-8");
                        }
                        if(key != null && value != null && value.length > 0){
                            questMap.put(key, value);
                            break;
                        }
                    }
                }
            }
        }
        return questMap;
    }

    public HashMap<String, byte[]> exQuestSE(HashMap<String, byte[]> exMap) throws Exception{
        HashMap<String, byte[]> sourceMap = exQuestCN();
        if( pathToIndexSE == null )return exMap;
        HashMap<Integer, SqPackIndexFolder> indexSE = new SqPackIndex(pathToIndexSE).resloveIndex();
        // 根据传入的文件进行遍历
        int fileCount = 0;
        String replaceFile = "EXD/Quest";
        // 准备好文件目录名和文件名
        String filePatch = replaceFile.substring(0, replaceFile.lastIndexOf("/"));
        String fileName = replaceFile.substring(replaceFile.lastIndexOf("/") + 1) + ".EXH";
        // 计算文件目录CRC
        Integer filePatchCRC = FFCRC.ComputeCRC(filePatch.toLowerCase().getBytes());
        // 计算头文件CRC
        Integer exhFileCRC = FFCRC.ComputeCRC(fileName.toLowerCase().getBytes());
        // 解压并且解析头文件
        if (indexSE.get(filePatchCRC) == null) return exMap;
        SqPackIndexFile exhIndexFileSE = indexSE.get(filePatchCRC).getFiles().get(exhFileCRC);
        if (exhIndexFileSE == null) return exMap;
        byte[] exhFileSE = extractFile(pathToIndexSE, exhIndexFileSE.getOffset());
        EXHFFile exhSE = new EXHFFile(exhFileSE);
        if (exhSE.getLangs().length > 0) {
            // 根据头文件 轮询资源文件
            for (EXDFPage exdfPage : exhSE.getPages()) {
                // 获取资源文件的CRC
                Integer exdFileCRCJA = FFCRC.ComputeCRC((fileName.replace(".EXH", "_" + String.valueOf(exdfPage.pageNum) + "_JA.EXD")).toLowerCase().getBytes());
                // 提取对应的文本文件
                SqPackIndexFile exdIndexFileJA = indexSE.get(filePatchCRC).getFiles().get(exdFileCRCJA);
                byte[] exdFileJA = null;
                try {
                    exdFileJA = extractFile(pathToIndexSE, exdIndexFileJA.getOffset());
                } catch (Exception jaEXDFileException) {
                    continue;
                }
                // 解压本文文件 提取内容
                EXDFFile ja_exd = new EXDFFile(exdFileJA);
                HashMap<Integer, byte[]> jaExdList = ja_exd.getEntrys();
                for (Map.Entry<Integer, byte[]> listEntry : jaExdList.entrySet()) {
                    Integer listEntryIndex = listEntry.getKey();
                    EXDFEntry exdfEntryJA = new EXDFEntry(listEntry.getValue(), exhSE.getDatasetChunkSize());
                    for (EXDFDataset exdfDatasetSE : exhSE.getDatasets()) {
                        // 只限文本内容
                        if (exdfDatasetSE.type == 0x0 && exdfDatasetSE.offset == 0x968) {
                            String key = new String(exdfEntryJA.getString(exdfDatasetSE.offset),"UTF-8");
                            if(sourceMap.get(key) != null){
                                exMap.put("Quest_".toLowerCase() + String.valueOf(listEntryIndex) + "_1",sourceMap.get(key));
                            }
                            break;
                        }
                    }
                }
            }
        }
        return exMap;
    }

    private byte[] extractFile(String pathToIndexSE, long dataOffset) throws IOException, FileNotFoundException {
        String pathToOpen = pathToIndexSE;
        int datNum = (int) ((dataOffset & 0xF) / 2L);
        dataOffset -= (dataOffset & 0xF);
        pathToOpen = pathToOpen.replace("index2", "dat" + datNum);
        pathToOpen = pathToOpen.replace("index", "dat" + datNum);
        SqPackDatFile datFile = new SqPackDatFile(pathToOpen);
        byte[] data = datFile.extractFile(dataOffset * 8L, false);
        datFile.close();
        return data;
    }
}
