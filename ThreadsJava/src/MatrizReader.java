// MatrizReader.java
import java.io.*;
import java.nio.*;

public class MatrizReader {

    private int n;
    private int[] dados;

    public MatrizReader(String caminho) throws Exception {
        FileInputStream fis = new FileInputStream(caminho);

        byte[] headerBytes = fis.readNBytes(8);
        ByteBuffer headerBuffer = ByteBuffer
                .wrap(headerBytes)
                .order(ByteOrder.LITTLE_ENDIAN);

        n = headerBuffer.getInt();
        headerBuffer.getInt();

        byte[] dataBytes = fis.readAllBytes();
        fis.close();

        IntBuffer intBuffer = ByteBuffer
                .wrap(dataBytes)
                .order(ByteOrder.LITTLE_ENDIAN)
                .asIntBuffer();

        dados = new int[n * n];
        intBuffer.get(dados);
    }

    public int getN() {
        return n;
    }

    public int[] getDados() {
        return dados;
    }
}