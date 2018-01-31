package link.teemo.sqpack;

import com.shenou.fs.core.utils.res.Config;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import link.teemo.sqpack.util.ArrayUtil;
import link.teemo.sqpack.util.FFXIVString;
import link.teemo.sqpack.util.HexUtils;

import java.io.File;

public class TranstringTest {
    public static void main(String[] args) throws  Exception{
        byte[] br = HexUtils.hexStringToBytes("02100103");
        //技能不能注册为快捷键
        //030210010302100103021306FEFFFFFF6603E280BBE8BF99E4B8AAE68A80E883BDE4B88DE883BDE6B3A8E5868CE4B8BAE5BFABE68DB7E994AE021302EC03
        //0210010302100103021306FEFFFFFF6603E280BBE8AFA5E68A80E883BDE697A0E6B395E8AEBEE7BDAEE588B0E783ADE994AEE6A08F021302EC03
        //固定咏唱
        //02100103E8BF99E9AD94E6B395E69C89E59BBAE69C89E79A84E5868DE5928FE594B1E697B6E997B4
        //02100103E6ADA4E9AD94E6B395E69C89E59BBAE69C89E79A84E5868DE5928FE594B1E697B6E997B4
        String test = "陷于黑暗之中，命中率降低";
        //空中霸王
        //达达鲁玛
        String test2 = "红色";
        String hexStr = "E6B0B4E6B3A5E99984E79D80E78AB6E68081EFBC8CE6ADA3E59CA8E98090E6B890E79FB3E58C96E38082E59CA8E69588E69E9CE7BB93E69D9FE697B6E8BF9BE585A5E79FB3E58C96E78AB6E68081";
        System.out.println(hexStr);
        System.out.println(HexUtils.bytesToHexStringWithOutSpace(test.getBytes("UTF-8")));
        System.out.println(FFXIVString.parseFFXIVString(HexUtils.hexStringToBytes(hexStr)));
        hexStr = hexStr.replace(HexUtils.bytesToHexStringWithOutSpace(test.getBytes("UTF-8")),HexUtils.bytesToHexStringWithOutSpace(test2.getBytes("UTF-8")));
        System.out.println(hexStr);
        System.out.println(FFXIVString.parseFFXIVString(HexUtils.hexStringToBytes(hexStr)));
    }
}
