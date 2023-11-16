//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.util.HashMap;
//
//public class Compresser implements IHuffConstants {
//    int headerFormat;
//    HashMap<Integer, String> map;
//    HuffmanTree storeTree;
//    private final int[] freq;
//
//    public Compresser(int[] freq, int headerFormat, HuffmanTree storeTree) {
//        this.freq = freq;
//        this.headerFormat = headerFormat;
//        this.storeTree = storeTree;  // Initialize HuffmanTree here
//        map = storeTree.buildCode();
//    }
//
//    public int compressKickoff(InputStream in, OutputStream out, int headerFormat) throws IOException {
//        int bitsRead = 0;
//        BitInputStream bitIn = new BitInputStream(in);
//        BitOutputStream bitOut = new BitOutputStream(out);
//
//        // Write the magic number
//        bitOut.writeBits(BITS_PER_INT, MAGIC_NUMBER);
//        bitsRead += BITS_PER_INT;
//
//        // Write the header format
//        bitOut.writeBits(BITS_PER_INT, headerFormat);
//        bitsRead += BITS_PER_INT;
//
//        // Write the tree structure to the BitOutputStream
//        storeTree.writeHeader(bitOut);
//        bitsRead += storeTree.sizeEncode();
//
//        // Read data and write to BitOutputStream
//        int inBits = bitIn.readBits(BITS_PER_WORD);
//        while (inBits != -1) {
//            String code = map.get(inBits);
//            if (code != null) {
//                bitOut.writeBits(code.length(), Integer.parseInt(code, 2));
//                bitsRead += code.length();
//            } else {
//                throw new IOException("Error in reading data");
//            }
//
//            // Read the next bits for the next iteration
//            inBits = bitIn.readBits(BITS_PER_WORD);
//        }
//
//        // Write PSEUDO_EOF
//        String pseofCode = map.get(PSEUDO_EOF);
//        if (pseofCode != null) {
//            bitOut.writeBits(pseofCode.length(), Integer.parseInt(pseofCode, 2));
//            bitsRead += pseofCode.length();
//        } else {
//            throw new IOException("PSEUDO_EOF code not found");
//        }
//
//        // Don't close bitOut here, let it be closed outside this method
//        // bitOut.close();
//        bitIn.close();
//        return bitsRead;
//    }
//
//
//
//    private int headerBits(BitOutputStream out) throws IOException {
//        int bitsRead = 0;
//        if (headerFormat == STORE_COUNTS) {
//            for (int j : freq) {
//                out.writeBits(BITS_PER_INT, j);
//                bitsRead += BITS_PER_INT;
//            }
//        } else if (headerFormat == STORE_TREE) {
//            bitsRead += storeTree.writeHeader(out);
//        } else {
//            throw new IOException("Not a valid headerFormat");
//        }
//        // Close the BitOutputStream only after writing all bits
//        // It should not be closed here
//        // out.close();
//        return bitsRead;
//    }
//}
