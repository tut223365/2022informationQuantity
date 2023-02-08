package s4.B223312;

import java.util.ArrayList;
import java.util.Stack;

public class SuffixNode {
    SuffixNode parent;
    ArrayList<SuffixNode> childs;

    int lcp, l, r;

    public SuffixNode(int lcp, int l, int r) {
        this.lcp = lcp;
        this.l = l;
        this.r = r;
        this.childs = new ArrayList<>();
    }
    public void addChild(SuffixNode child) {
        childs.add(child);
        child.parent = this;
    }
    public void setLastChild(SuffixNode child) {
        childs.set(childs.size() - 1, child);
        child.parent = this;
    }

    int length() {
        return r - l;
    }
    boolean isRoot() {
        return parent == null;
    }
    boolean isLeaf() {
        return lcp == -1;
    }
    void printSuffixNode() {
        System.out.println("lcp = " + lcp + "[" + l + ", " + r + ") chcnt = " + childs.size());
        
        for (int i = 0; i < childs.size(); ++i) {
            childs.get(i).printSuffixNode();
        }
    }
}
