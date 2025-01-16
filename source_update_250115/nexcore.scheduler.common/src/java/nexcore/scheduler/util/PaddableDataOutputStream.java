package nexcore.scheduler.util;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : </li>
 * <li>설  명 : </li>
 * <li>작성일 : 2009. 8. 25.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 * byte[] 전문 구성에서 자리수를 맞추기 위해 padding 기능을 추가된 DataOutputStream.
 * byte[] 전문구성시 필요한 padding 기능을 추가한 DataOutputStream 클래스.
 */
public class PaddableDataOutputStream extends DataOutputStream {
    private String encoding;

    public PaddableDataOutputStream(OutputStream out) {
        super(out);
        encoding = System.getProperty("file.encoding");
    }

    public PaddableDataOutputStream(OutputStream out, String encoding) {
        super(out);
        this.encoding = encoding;
    }

    /**
     * String 을 getBytes() 하여 byte[] 로 OutputStream 으로 전송.
     * @param s
     * @throws IOException
     */
    public void writeString(String s) throws IOException {
        if (s==null) return; // null 일때는 아무것도 write하지 않음
        super.write(s.getBytes(encoding));
    }

    /**
     * String을 getBytes() 하여 write할때, 먼저 길이를 int4로 write한 후에 String 내용 write. 
     * @param s
     * @throws IOException
     */
    public void writeStringWithIntLength(String s) throws IOException {
        if (s==null) {
            super.writeInt(-1);
        }else {
            byte[] b = s.getBytes(encoding);
            super.writeInt(b.length);
            super.write(b);
        }
    }

    /**
     * String을 getBytes() 하여 write할때, 지정된 길이만큼 write. 
     * str의 길이가 length 보다 작을 경우 padbyte 문자로 왼쪽에 패딩하며,
     * str의 길이가 length 보가 클 경우, 오른쪽의 문자를 잘라내서 length 에 맞춤. 
     * @param str
     * @param length
     * @param padByte
     * @throws IOException
     */
    public void writeStringWithLPadding(String str, int length, byte padByte) throws IOException {
        _writeStringWithPadding(str, length, padByte, true, false, false);
    }

    /**
     * String을 getBytes() 하여 write할때, 지정된 길이만큼 write. 
     * str의 길이가 length 보다 작을 경우 padbyte 문자로 오른쪽에 패딩하며,
     * str의 길이가 length 보가 클 경우, 오른쪽의 문자를 잘라내서 length 에 맞춤. 
     * @param str
     * @param length
     * @param padByte
     * @throws IOException
     */
    public void writeStringWithRPadding(String str, int length, byte padByte) throws IOException {
        _writeStringWithPadding(str, length, padByte, false, true, false);
    }

    /**
     * String을 getBytes() 하여 write할때, 지정된 길이만큼 write. 
     * str의 길이가 length 보다 작을 경우 padbyte 문자로 왼쪽,오른쪽에 절반씩 패딩하며 (center-align),
     * str의 길이가 length 보가 클 경우, 오른쪽의 문자를 잘라내서 length 에 맞춤. 
     * @param str
     * @param length
     * @param padByte
     * @throws IOException
     */
    public void writeStringWithLRPadding(String str, int length, byte padByte) throws IOException {
        _writeStringWithPadding(str, length, padByte, false, false, true);
    }

    private void _writeStringWithPadding(String str, int length, byte padByte, boolean isLPad, boolean isRPad, boolean isLRPad) throws IOException {
        byte[] data = ( str==null ) ? new byte[0] : str.getBytes(encoding);

        int diff = length - data.length;
        if (diff > 0) {
            if (isLPad) {
                for (int i=1; i<=diff; i++) super.write(padByte); // L padding
                super.write(data);
            }else if (isRPad) {
                super.write(data);
                for (int i=1; i<=diff; i++) super.write(padByte); // R padding
            }else if (isLRPad) {
                for (int i=1; i<=diff; i++) {
                    super.write(padByte); // LR padding (1/2)
                    if (i == diff/2) super.write(data);
                }
            }
        }else if (diff < 0) {
            super.write(data, 0, length); // cutting
        }else {
            super.write(data);
        }
    }
}
