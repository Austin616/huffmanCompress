import java.io.IOException;
import java.util.HashMap;
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
public class HuffmanTree implements IHuffConstants {
    private PQueue<TreeNode> queue;
    private TreeNode root;
    private int size;
    private int numLeafNodes;
    private int totalNodes;

    public HuffmanTree(int[] freq) {
        // Check for null or empty frequency array
        if (freq == null || freq.length < 1) {
            throw new IllegalArgumentException("Invalid frequency array");
        }
        queue = new PQueue<TreeNode>();
        buildTree(freq, queue);
    }

    // builds a huffman tree using the STF format
    public HuffmanTree(BitInputStream in) throws IOException{
        size = in.readBits(BITS_PER_INT);
        root = rebuildRecursive(in);
    }
    public void buildTree(int[] freq, PQueue<TreeNode> queue) {
        // Builds queue
        for (int i = 0; i < freq.length; i++) {
            if (freq[i] > 0) {
                TreeNode value = new TreeNode(i, freq[i]);
                queue.enqueue(value);
                // Each leaf node takes up one bit to indicate whether it has a value,
                // BITS_PER_WORD + 1
                // to account for the original ALPH_SIZE with the addition of the eof.
                size += 1 + (BITS_PER_WORD + 1);
            }
        }
        // Builds tree
        while (queue.size() > 1) {
            TreeNode left = queue.dequeue();
            TreeNode right = queue.dequeue();
            TreeNode newNode = new TreeNode(left, left.getFrequency() + right.getFrequency(), right);
            // Each new internal node requires 1 bit to indicate it does not contain a value.
            size++;
            queue.enqueue(newNode);
        }
        root = queue.dequeue();
    }

    // returns the number of bits required to encode the Huffman tree.
    public int sizeEncode(){
        return size;
    }

    // This method builds the path for each value in the tree
    public HashMap<Integer,String> buildCode(){
        HashMap<Integer,String> map = new HashMap<>();
        encode(root,"",map);
        return map;
    }

    // Recursive method that buildCode() uses to get the path
    public void encode(TreeNode node, String path, HashMap<Integer, String> map) {
        totalNodes++;
        if (node.isLeaf()) {
            numLeafNodes++;
            map.put(node.getValue(), path);
        } else {
            encode(node.getLeft(), path + "0", map);
            encode(node.getRight(), path + "1", map);
        }
    }

    // Uses the pseudocode from bottom of TA pdf to build new tree from STF format
    private TreeNode rebuildRecursive(BitInputStream in)
            throws IOException {
        int bit = in.readBits(1);
        // internal node so construct a two child nodes for it
        if (bit == 0) {
            return new TreeNode(rebuildRecursive(in),
                    0, rebuildRecursive(in));
            // reached a leave node so create node with the value stored
        } else if (bit == 1) {
            int value = in.readBits(9);
            return new TreeNode(value, 0);
        } else {
            throw new IOException("Incorrect format of input file.");
        }
    }

    // This method writes out the bits during a preorder traversal of the tree
    public void preorderTraversal(BitOutputStream bitOut, TreeNode node) {
        if (node.isLeaf()) {
            // Leaf node is represented by 1 followed by 9 bits for the value.
            bitOut.writeBits(1, 1);
            bitOut.writeBits(BITS_PER_WORD + 1, node.getValue());
        } else {
            // Internal node is represented by 0.
            bitOut.writeBits(1, 0);
            // Recursive traversal for left and right children.
            preorderTraversal(bitOut, node.getLeft());
            preorderTraversal(bitOut, node.getRight());
        }
    }

    // gets num of leaf nodes
    public int getLeaf(){
        return numLeafNodes;
    }

    // gets num of total nodes
    public int getTotal(){
        return totalNodes;
    }

    // returns the root node
    public TreeNode getRoot(){
        return root;
    }
}
