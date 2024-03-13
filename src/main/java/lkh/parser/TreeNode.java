package lkh.parser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.io.PrintWriter;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TreeNode {
    private TokenType tokenType;
    private String name;
    private TreeNode left;
    private TreeNode right;
    private int size;

    public TreeNode(TokenType tokenType, String name) {
        this(tokenType, name, null, null);
    }

    public TreeNode(TokenType tokenType, String name, TreeNode left, TreeNode right) {
        this.tokenType = tokenType;
        this.name=name;
        this.left=left;
        this.right=right;

        this.size = 1;
        if (left != null)
            this.size += left.size;
        if (right != null)
            this.size += right.size;
    }

    public void toDotFile(String filename) {
        try {
            PrintWriter f = new PrintWriter(filename);
            f.print("digraph Tree{\n");
            writeSons(f, this, 0);
            f.print("}");
            f.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    private void writeSons(PrintWriter f, TreeNode tree, int id) {
        writeLabel(f,tree, id);
        if(tree.left != null){
            int left_id = id + 1;
            f.printf("%d -> %d;\n", id, left_id);
            writeSons(f, tree.left, left_id);
        }
        if(tree.right != null){
            int right_id = id + 1;
            if (tree.left != null)
                right_id += tree.left.size;
            f.printf("%d -> %d;\n", id, right_id);
            writeSons(f, tree.right, right_id);
        }
    }

    private void writeLabel(PrintWriter f, TreeNode tree, int id) {
        f.printf("%d [label=\"%s\"];\n", id, tree.name);
    }

}