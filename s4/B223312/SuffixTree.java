package s4.B223312;

import java.util.Stack;
import java.util.ArrayList;

public class SuffixTree {
    /*
     * どういうデータ構造なのかは分かっているが，構築方法がいまいちよくわからなかったので，ガチャガチャやっている。
     * LCP 配列に対して Interval Tree っぽいものを構築したあとに，葉ノードを付け足すという 2 パス構築で実装した。
     * 各ノードに高々 σ 個の子がある。検索は各ノードで二分探索することで O(|T|log|σ|)
     */

    byte[] str;

    int[] suffixArray;
    int[] lcpArray;

    SuffixNode root;
    SuffixNode emptyNode = new SuffixNode(-1, -1, -1);

    private void addLeaves() {
        Stack<SuffixNode> stack = new Stack<>();
        stack.push(root);

        while (!stack.empty()) {
            SuffixNode node = stack.pop();

            int idx = node.l;
            ArrayList<SuffixNode> fullyChilds = new ArrayList<>();
            for (int i = 0; i < node.childs.size(); ++i) {
                SuffixNode child = node.childs.get(i);
    
                while (idx < child.l) {
                    fullyChilds.add(new SuffixNode(-1, idx, idx + 1));
                    idx++;
                }
                fullyChilds.add(child);
                stack.push(child);
    
                idx = child.r;
            }
            
            while (idx < node.r) {
                fullyChilds.add(new SuffixNode(-1, idx, idx + 1));
                idx++;
            }
    
            node.childs = fullyChilds;
        }
    }
    public SuffixTree(byte[] str) {
        this.str = str;

        SuffixArray tmp = new SuffixArray(str);
        this.suffixArray = tmp.suffixArray;
        this.lcpArray = tmp.lcp;

        this.root = new SuffixNode(0, 0, suffixArray.length);

        Stack<SuffixNode> stack = new Stack<>();
        stack.add(root);
        for (int i = 2; i < suffixArray.length; ++i) {
            int lcp = lcpArray[i]; // lcp(sa[i - 1], sa[i])
            int nodelcp = stack.peek().lcp;

            if (nodelcp < lcp) {
                SuffixNode node = new SuffixNode(lcp, i - 1, suffixArray.length);
                stack.peek().addChild(node);
                stack.push(node);
            } else if (nodelcp > lcp) {
                int left = i - 1;
                SuffixNode child = null;
                while (stack.peek().lcp > lcp) {
                    child = stack.pop();
                    child.r = i;
                    left = child.l;
                }

                SuffixNode parent = stack.peek();
                if (parent.lcp == lcp) continue;
                
                SuffixNode node = new SuffixNode(lcp, left, suffixArray.length);
                parent.setLastChild(node);
                node.addChild(child);
                stack.push(node);
            }
        }

        addLeaves();
    }
    SuffixNode search(SuffixNode node, Byte c, int strIdx) {
        int key = Byte.toUnsignedInt(c);

        if (node.length() == 0) return node;
        if (node.lcp == strIdx) return searchChild(node, key, strIdx);
        return searchEdge(node, key, strIdx);
    }
    SuffixNode searchChild(SuffixNode node, int key, int strIdx) {
        int ok = node.childs.size() - 1, ng = -1;
        while (ok - ng > 1) {
            int mid = (ok + ng) >> 1;
            
            SuffixNode child = node.childs.get(mid);
            int sa = suffixArray[child.l];
            int ch = (sa + strIdx < str.length ? Byte.toUnsignedInt(str[sa + strIdx]) : -1);
            
            if (key <= ch) ok = mid;
            else ng = mid;
        }

        node = node.childs.get(ok);
        
        int sa = suffixArray[node.l];
        int ch = (sa + strIdx < str.length ? Byte.toUnsignedInt(str[sa + strIdx]) : -1);
        return (ch == key ? node : emptyNode);
    }
    SuffixNode searchEdge(SuffixNode node, int key, int strIdx) {
        int sa = suffixArray[node.l];
        int ch = (sa + strIdx < str.length ? Byte.toUnsignedInt(str[sa + strIdx]) : -1);
        return (ch == key ? node : emptyNode);
    }

    void printSuffixTree() {
        root.printSuffixNode();
    }
}
