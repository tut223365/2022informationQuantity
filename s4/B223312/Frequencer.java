package s4.B223312;

import java.util.Arrays;
import s4.specification.*;

/*
interface FrequencerInterface {  // This interface provides the design for frequency counter.
    void setTarget(byte[] target);  // set the data to search.
    void setSpace(byte[] space);  // set the data to be searched target from.
    int frequency(); // It return -1, when TARGET is not set or TARGET's length is zero
                     // Otherwise, it return 0, when SPACE is not set or Space's length is zero
                     // Otherwise, get the frequency of TAGET in SPACE
    int subByteFrequency(int start, int end);
    // get the frequency of subByte of taget, i.e. target[start], taget[start+1], ... , target[end-1].
    // For the incorrect value of START or END, the behavior is undefined.
}
*/

public class Frequencer implements FrequencerInterface {
    static boolean debugMode = false;
    byte[] myTarget;
    byte[] mySpace;

    boolean useSlowSuffixArray = true;
    int[] suffixArray_slow;
    SuffixArray suffixArray;

    private int suffixCompare(int i, int j) {
        int length = mySpace.length - (i > j ? i : j);
        for (int cur = 0; cur < length; ++cur) {
            if (mySpace[i + cur] == mySpace[j + cur])
                continue;

            return Byte.toUnsignedInt(mySpace[i + cur]) - Byte.toUnsignedInt(mySpace[j + cur]);
        }

        return j - i;
    }
    private void mergeSortSuffixArray(int l, int r) {
        if (l + 1 == r)
            return;

        int mid = (l + r) >> 1;
        mergeSortSuffixArray(l, mid);
        mergeSortSuffixArray(mid, r);

        int[] tmp = new int[r - l];
        for (int i = 0, li = l, ri = mid; i < tmp.length; ++i) {
            if (li < mid && ri < r) {
                if (suffixCompare(suffixArray_slow[li], suffixArray_slow[ri]) < 0) {
                    tmp[i] = suffixArray_slow[li++];
                } else {
                    tmp[i] = suffixArray_slow[ri++];
                }
            } else if (li < mid) {
                tmp[i] = suffixArray_slow[li++];
            } else {
                tmp[i] = suffixArray_slow[ri++];
            }
        }

        for (int i = 0; i < tmp.length; ++i)
            suffixArray_slow[i + l] = tmp[i];
    }

    public void setSpace(byte[] space) {
        mySpace = space;

        if (useSlowSuffixArray) {
            int spaceLength = space.length;
            suffixArray_slow = new int[spaceLength + 1];

            for (int i = 0; i < suffixArray_slow.length; ++i)
                suffixArray_slow[i] = i;
            mergeSortSuffixArray(0, suffixArray_slow.length);
        } else {
            suffixArray = new SuffixArray(space);
        }
    }
    public void setTarget(byte[] target) {
        myTarget = target;
    }

    private void showVariables() {
        for (int i = 0; i < mySpace.length; i++) {
            System.out.write(mySpace[i]);
        }
        System.out.write(' ');
        for (int i = 0; i < myTarget.length; i++) {
            System.out.write(myTarget[i]);
        }
        System.out.write(' ');
    }
    public void printSuffixArray() {
        if (useSlowSuffixArray) {
            System.out.println("useSlowSuffixArray = true");
            for (int i = 0; i < suffixArray_slow.length; i++) {
                int s = suffixArray_slow[i];
                System.out.printf("suffixArray[%2d]=%2d:", i, s);
                for (int j = s; j < mySpace.length; j++) {
                    System.out.write(mySpace[j]);
                }
                System.out.write('\n');
            }
        } else {
            suffixArray.printSuffixArray();
        }
    }

    public int frequency() {
        if (myTarget == null || myTarget.length == 0) return -1;
        if (mySpace == null) return 0;
        
        if (debugMode) {
            showVariables();
        }

        return subByteFrequency(0, myTarget.length);
    }

    private int targetCompare(int idx, byte[] target) {
        int spaceLength = mySpace.length;

        int sa = suffixArray_slow[idx];
        for (int i = 0; i < target.length; ++i) {
            if (sa + i >= spaceLength) return -1;
            if (mySpace[sa + i] == target[i]) continue;
            
            return Byte.toUnsignedInt(mySpace[sa + i]) - Byte.toUnsignedInt(target[i]);
        }
        return 0;
    }
    private int searchLowerBound(byte[] target) {
        int lo = 0, hi = suffixArray_slow.length;
        while (hi - lo > 1) {
            int mid = (hi + lo) >> 1;

            if (targetCompare(mid, target) < 0) lo = mid;
            else hi = mid;
        }
        return lo + 1;
    }
    private int searchUpperBound(byte[] target) {
        int lo = 0, hi = suffixArray_slow.length;
        while (hi - lo > 1) {
            int mid = (hi + lo) >> 1;

            if (targetCompare(mid, target) <= 0) lo = mid;
            else hi = mid;
        }
        return lo + 1;
    }
    public int subByteFrequency(int start, int length) {
        byte[] subByte = Arrays.copyOfRange(myTarget, start, length);

        int first = 0, last = 1;
        if (useSlowSuffixArray) {
            first = searchLowerBound(subByte);
            last = searchUpperBound(subByte);
        } else {
            first = suffixArray.searchLowerBound(subByte);
            last = suffixArray.searchUpperBound(subByte);
        }
        return last - first;
    }

    public static void main(String[] args) {
        Frequencer frequencerObject;
        try { // テストに使うのに推奨するmySpaceの文字は、"ABC", "CBA", "HHH", "Hi Ho Hi Ho".
            frequencerObject = new Frequencer();
            frequencerObject.setSpace("ABC".getBytes());
            frequencerObject.printSuffixArray();
            frequencerObject = new Frequencer();
            frequencerObject.setSpace("CBA".getBytes());
            frequencerObject.printSuffixArray();
            frequencerObject = new Frequencer();
            frequencerObject.setSpace("HHH".getBytes());
            frequencerObject.printSuffixArray();
            frequencerObject = new Frequencer();
            frequencerObject.setSpace("Hi Ho Hi Ho".getBytes());
            frequencerObject.printSuffixArray();
            /* Example from "Hi Ho Hi Ho"    
               0: Hi Ho                      
               1: Ho                         
               2: Ho Hi Ho                   
               3:Hi Ho                       
               4:Hi Ho Hi Ho                 
               5:Ho                          
               6:Ho Hi Ho
               7:i Ho                        
               8:i Ho Hi Ho                  
               9:o                           
              10:o Hi Ho                     
            */

            frequencerObject.setTarget("H".getBytes());
            int first = frequencerObject.suffixArray.searchLowerBound(frequencerObject.myTarget);
            int last = frequencerObject.suffixArray.searchUpperBound(frequencerObject.myTarget);
            System.out.print("Search[" + first + ", " + last + ") ");
            if (4 == first && 8 == last) { System.out.println("OK"); } else {System.out.println("WRONG"); }

            int result = frequencerObject.frequency();
            System.out.print("Freq = "+ result+" ");
            if(4 == result) { System.out.println("OK"); } else {System.out.println("WRONG"); }
        }
        catch(Exception e) {
            System.out.println("STOP");
        }
    }
}
