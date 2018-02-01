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
        String test = "<br>";
        //空中霸王
        //达达鲁玛
        String test2 = "点伤害！";
        String hexStr = "E8BF99E698AFE4B880E7A78DE8AEBEE8AEA1E587BAE69DA5E588A9E794A8E4B889E69697E7A59EE4B98BE58A9BE79A84E696B9E6B395E299AA02100103E8AEA9E68891E4BBACE4BBA5E9AD94E7A59EE890A8E88FB2E6B49BE789B9E4B8BAE4BE8BE5AD90E69DA5E5B195E7A4BAE79C8BE79C8BE590A7E299AA";
        System.out.println(hexStr);
        System.out.println(HexUtils.bytesToHexStringWithOutSpace(test.getBytes("UTF-8")));
        System.out.println(FFXIVString.parseFFXIVString(HexUtils.hexStringToBytes(hexStr)));
        hexStr = hexStr.replace(HexUtils.bytesToHexStringWithOutSpace(test.getBytes("UTF-8")),HexUtils.bytesToHexStringWithOutSpace(test2.getBytes("UTF-8")));
        System.out.println(hexStr);
        System.out.println(FFXIVString.parseFFXIVString(HexUtils.hexStringToBytes(hexStr)));
    }
}
