/* Student information for assignment:
 *
 * On <OUR> honor, <Austin> and <Micayla>, this programming assignment is <OUR> own work
 * and <WE> have not provided this code to any other student.
 *
 * Number of slip days used:
 *
 * Student 1 (Student whose Canvas account is being used)
 * UTEID: AAT3377
 * email address: austintran616@gmail.com
 * Grader name: Sai
 *
 * Student 2: Micayla Bustillos
 * UTEID: mvb567
 * email address: micayla.bustillos@utexas.edu
 *
 */

import java.io.*;
import java.util.HashMap;

public class SimpleHuffProcessor implements IHuffProcessor {
    HuffmanTree tree;
    int headerFormat;
    private IHuffViewer myViewer;
    private int[] freq;
    private HashMap<Integer, String> map;
    private int preCompress;
    private int compress;
    private int bitsSaved;

    /**
     * Preprocess data so that compression is possible ---
     * count characters/create tree/store state so that
     * a subsequent call to compress will work. The InputStream
     * is <em>not</em> a BitInputStream, so wrap it int one as needed.
     *
     * @param in           is the stream which could be subsequently compressed
     * @param headerFormat a constant from IHuffProcessor that determines what kind of
     *                     header to use, standard count format, standard tree format, or
     *                     possibly some format added in the future.
     * @return number of bits saved by compression or some other measure
     * Note, to determine the number of
     * bits saved, the number of bits written includes
     * ALL bits that will be written including the
     * magic number, the header format number, the header to
     * reproduce the tree, AND the actual data.
     * @throws IOException if an error occurs while reading from the input file.
     */
    public int preprocessCompress(InputStream in, int headerFormat) throws IOException {
        if (headerFormat != STORE_COUNTS && headerFormat != STORE_TREE) {
            myViewer.showError("Not in correct format.");
        }
        this.headerFormat = headerFormat;
        freq = new int[ALPH_SIZE + 1]; // 256 + 1 aka a spot for (PEOF VALUE)
        freq[ALPH_SIZE] = 1;
        preCompress = getFrequencies(new BitInputStream(new BitInputStream(in)));
        tree = new HuffmanTree(freq);
        map = tree.buildCode();
        // Calculate compression cost
        compress = calcBits(headerFormat);
        bitsSaved = preCompress - compress;
        return bitsSaved;
    }

    // This method gets the compression cost
    private int calcBits(int headerFormat) {
        // The magic number and header format are both integers, add their bits.
        int compressionBits = BITS_PER_INT * 2;
        if (headerFormat == STORE_COUNTS) {
            // Calculate the additional compression cost for storing frequency counts.
            compressionBits += ALPH_SIZE * BITS_PER_INT;
        } else if (headerFormat == STORE_TREE) {
            // Calculate the additional compression cost for storing the Huffman tree.
            compressionBits += tree.sizeEncode() + BITS_PER_INT;
        }
        compressionBits += getCompDataTotal();
        return compressionBits;
    }

    // this method calculates the total number of bits needed to represent the compressed data
    private int getCompDataTotal() {
        int total = 0;
        for (int i = 0; i < freq.length; i++) {
            String value = map.get(i);
            if (value != null) {
                total += freq[i] * value.length();
            }
        }
        return total;
    }

    // Returns the number of words * BITS_PER_WORD of each letter
    private int getFrequencies(BitInputStream bits) throws IOException {
        if (this.freq == null) {
            throw new IllegalStateException("freq array is not initialized");
        }
        int inbits = bits.readBits(BITS_PER_WORD);
        int totalBits = 0;
        while (inbits != -1) {
            this.freq[inbits]++;
            totalBits += BITS_PER_WORD;
            inbits = bits.readBits(BITS_PER_WORD);
        }
        return totalBits;
    }

    /**
     * Compresses input to output, where the same InputStream has
     * previously been pre-processed via <code>preprocessCompress</code>
     * storing state used by this call.
     * <br> pre: <code>preprocessCompress</code> must be called before this method
     *
     * @param in    is the stream being compressed (NOT a BitInputStream)
     * @param out   is bound to a file/stream to which bits are written
     *              for the compressed file (not a BitOutputStream)
     * @param force if this is true create the output file even if it is larger than the input file.
     *              If this is false do not create the output file if it is larger than the input file.
     * @return the number of bits written.
     * @throws IOException if an error occurs while reading from the input file or
     *                     writing to the output file.
     */
    public int compress(InputStream in, OutputStream out, boolean force) throws IOException {
        if (!force && bitsSaved < 0) {
            myViewer.showMessage("Error in compressing file. Too big to compress.");
            return -1;
        }
        BitInputStream bitIn = new BitInputStream(new BufferedInputStream(in));
        BitOutputStream bitOut = new BitOutputStream(new BufferedOutputStream(out));
        // writes magic number && header format
        bitOut.writeBits(BITS_PER_INT, MAGIC_NUMBER);
        bitOut.writeBits(BITS_PER_INT, headerFormat);
        // writes out the header content
        headerBits(bitOut);
        int inBits = bitIn.readBits(BITS_PER_WORD);
        // loops through the data and writes the data
        while (inBits != -1) {
            writeBits(inBits,bitOut,map);
            inBits = bitIn.readBits(BITS_PER_WORD);
        }
        // writes out the PSEUDO value
        writeBits(PSEUDO_EOF,bitOut,map);
        bitOut.close();
        bitIn.close();
        return compress;
    }

    // This helper method out the bits for the data passed in
    private void writeBits(int inBits, BitOutputStream bitOut, HashMap<Integer,String> path) {
        String code = path.get(inBits);
        // Loops through the path and writes the bit one at a time
        for (int i = 0; i < code.length(); i++) {
            char bit = code.charAt(i);
            if (bit == '0') {
                bitOut.writeBits(1, 0);
            } else {
                bitOut.writeBits(1, 1);
            }
        }
    }

    // Helper method to get the bits for the header content
    private int headerBits(BitOutputStream out) {
        if (headerFormat == STORE_COUNTS) {
            for (int i = 0; i < freq.length - 1; i++) {
                out.writeBits(BITS_PER_INT, freq[i]);
            }
        } else if (headerFormat == STORE_TREE) {
            int treeSize = (tree.getLeaf() * 9) + tree.getTotal();
            out.writeBits(BITS_PER_INT, treeSize);
            tree.preorderTraversal(out, tree.getRoot());
        }
        return compress;
    }

    /**
     * Uncompress a previously compressed stream in, writing the
     * uncompressed bits/data to out.
     *
     * @param in  is the previously compressed data (not a BitInputStream)
     * @param out is the uncompressed file/stream
     * @return the number of bits written to the uncompressed file/stream
     * @throws IOException if an error occurs while reading from the input file or
     *                     writing to the output file.
     */
    public int uncompress(InputStream in, OutputStream out) throws IOException {
        // Set up input and output streams
        BitInputStream bitIn = new BitInputStream(new BufferedInputStream(in));
        BitOutputStream bitOut = new BitOutputStream(new BufferedOutputStream(out));
        // Check for the magic number to ensure it's a Huffman encoded file
        if (bitIn.readBits(BITS_PER_INT) != MAGIC_NUMBER) {
            myViewer.showError("This file is not a Huffman encoded file.");
        }
        // Create the tree based on store counts header
        TreeNode position = treeBuilder(bitIn).getRoot();
        // Counter for all the bits written out
        int totalWritten = 0;
        // Flag to determine when decoding is done
        boolean done = false;
        while (!done) {
            int bit = bitIn.readBits(1);
            if (bit == -1) {
                throw new IOException("Error reading compressed file. No PSEUDO_EOF value.");
            }
            // Move left or right in the tree based on the current bit
            position = (bit == 0) ? position.getLeft() : position.getRight();
            if (position.isLeaf()) {
                // Increment the total bits written
                totalWritten += BITS_PER_WORD;
                if (position.getValue() == PSEUDO_EOF) {
                    done = true;
                } else {
                    // Write out the value and reset back to the root for the next traversal
                    bitOut.writeBits(BITS_PER_WORD, position.getValue());
                    position = tree.getRoot();
                }
            }
        }
        bitIn.close();
        bitOut.close();
        return totalWritten;
    }


    // This helper method builds the required tree based on the headerFormat
    private HuffmanTree treeBuilder(BitInputStream bitIn) throws IOException {
        // creates tree based on store counts header
        if (bitIn.readBits(BITS_PER_INT) == STORE_COUNTS) {
            for (int i = 0; i < ALPH_SIZE; i++) {
                int originalFreq = bitIn.readBits(BITS_PER_INT);
                freq[i] = originalFreq;
            }
            tree = new HuffmanTree(freq);
        } else {
            //creates tree using STF format
            tree = new HuffmanTree(bitIn);
        }
        return tree;
    }


    /**
     * Make sure this model communicates with some view.
     *
     * @param viewer is the view for communicating.
     */
    public void setViewer(IHuffViewer viewer) {
        myViewer = viewer;
    }

    private void showString(String s) {
        if (myViewer != null) {
            myViewer.update(s);
        }
    }
}
