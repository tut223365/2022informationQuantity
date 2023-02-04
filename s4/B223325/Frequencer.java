package s4.B223325;  // ここは、かならず、自分の名前に変えよ。
import java.lang.*;
import java.util.Random;
import java.util.Arrays;
import java.util.Comparator;

import s4.specification.*;


/*package s4.specification;
  ここは、１回、２回と変更のない外部仕様である。
  public interface FrequencerInterface {
    // set the data to search.
    void setTarget(byte  target[]); 

    // set the data to be searched target from.
    void setSpace(byte  space[]);  

    //It return -1, when TARGET is not set or TARGET's length is zero
    //Otherwise, it return 0, when SPACE is not set or SPACE's length is zero
    //Otherwise, get the frequency of TAGET in SPACE
    int frequency(); 

    // get the frequency of subByte of taget, i.e target[start], taget[start+1], ... , target[end-1].
    // For the incorrect value of START or END, the behavior is undefined.
    int subByteFrequency(int start, int end);
  }
*/



public class Frequencer implements FrequencerInterface{
    byte [] myTarget;
    byte [] mySpace;
    boolean targetReady = false;
    boolean spaceReady = false;
    // ソートされる suffixArray
    int []  suffixArray; 
    // ソートに利用する
    Random rand = new Random();
    // 新しい手法用
    int targetIndex = -1;
    int spaceOffset = -1;
    // デバッグ用
    boolean debugMode = true;

    // この関数は、デバッグに使ってもよい。mainから実行するときにも使ってよい。
    // リポジトリにpushするときには、main以外からは呼ばれないようにせよ。
    public void printSuffixArray() {
        if(spaceReady) {
            for(int i=0; i< mySpace.length; i++) {
                int s = suffixArray[i];
                System.out.printf("suffixArray[%2d]=%2d:", i, s);
                for(int j=s;j<mySpace.length;j++) {
                    System.out.write(mySpace[j]);
                }
                System.out.write('\n');
            }
        }
    }

    private void printSuffix(int i) {
        if(spaceReady) {
            int s = suffixArray[i];
            System.out.printf("suffixArray[%2d]=%2d:", i, s);
            for(int j=s;j<mySpace.length;j++) {
                System.out.write(mySpace[j]);
            }
            System.out.write('\n');
        }
    }

    private int suffixCompare(int i, int j) {
        // suffixCompareはソートのための比較メソッドである。
        // 次のように定義せよ。
        //
        // comparing two suffixes by dictionary order.
        // suffix_i is a string starting with the position i in "byte [] mySpace".
        // When mySpace is "ABCD", suffix_0 is "ABCD", suffix_1 is "BCD", 
        // suffix_2 is "CD", and sufffix_3 is "D".
        // Each i and j denote suffix_i, and suffix_j.                            
        // Example of dictionary order                                            
        // "i"      <  "o"        : compare by code                              
        // "Hi"     <  "Ho"       ; if head is same, compare the next element    
        // "Ho"     <  "Ho "      ; if the prefix is identical, longer string is big  
        //  
        //The return value of "int suffixCompare" is as follows. 
        // if suffix_i > suffix_j, it returns 1   
        // if suffix_i < suffix_j, it returns -1  
        // if suffix_i = suffix_j, it returns 0;   

        boolean endi = (i < this.mySpace.length);
        boolean endj = (j < this.mySpace.length);
        while (endi && endj) {
            if (this.mySpace[i] > this.mySpace[j]){
                return 1;
            } else if (this.mySpace[i] < this.mySpace[j]){
                return -1;
            }
            endi = (++i < this.mySpace.length);
            endj = (++j < this.mySpace.length);
        }
        if (!endi && !endj) {
            return 0;
        } else if (!endi) {
            return -1;
        } else { // else if (!endj)
            return 1;
        }
    }

    public void setSpace(byte[] space) { 
        // suffixArrayの前処理は、setSpaceで定義する
        mySpace = space; 
        if(mySpace.length>0){
            spaceReady = true;
        } else {
            spaceReady = false;
            return ;
        }
        Integer[] _suffixArray = new Integer[space.length];
        for(int i = 0; i< space.length; i++) {
            _suffixArray[i] = i; 
        }
        // 昇順のランダムクイックソート
        // スタック領域確保の処理がない分，（多少）高速
        // quickSort(0, suffixArray.length-1);
        
        // 並列マージソート
        Arrays.parallelSort(_suffixArray, new Comparator<Integer>() {
            @Override
            public int compare(Integer a, Integer b){
                return suffixCompare(a.intValue(), b.intValue());
            }
        });
        suffixArray = new int[space.length];
        for(int i = 0; i< space.length; i++) {
            suffixArray[i] = _suffixArray[i].intValue(); 
        }

    }

    /**
     * ランダムクイックソートで suffixArray をソートする．平均 O(n log n)
     * @param bottom x[bottom] が参照可能なインデックス．
     * @param top x[top] が参照可能なインデックス．初期値は length-1 など
     */
    void quickSort(int bottom, int top) {
        int lower = bottom, upper = top;
        int temp;
        int div_index = this.rand.nextInt(top-bottom+1)+bottom;
        int div = suffixArray[div_index];

        // div と bottom を交換
        temp = suffixArray[div_index];
        suffixArray[div_index] = suffixArray[bottom];
        suffixArray[bottom] = temp;
        div_index = bottom;

        // div を基準に分割
        // while true にすることで，そこには条件分岐がなく，高速
        while (true) {
            while ((lower <= upper) && (suffixCompare(suffixArray[lower], div) != 1))
                lower++;
            while ((lower <= upper) && (suffixCompare(suffixArray[upper], div) == 1))
                upper--;
            if (lower<upper) {
                temp = suffixArray[lower];
                suffixArray[lower] = suffixArray[upper];
                suffixArray[upper] = temp;
            } else {
                break;
            }
        }
        // div と upper を交換
        // upper は lower よりも小さいことが保証されている． 
        temp = suffixArray[bottom];
        suffixArray[bottom] = suffixArray[upper];
        suffixArray[upper] = temp;

        // 関数上部でまとめて処理するほうがコードは少なく済むが，処理はこちらのほうが少ない．
        // スタック領域確保の処理がないため
        if (bottom < upper-1)
            quickSort(bottom, upper - 1);
        if (upper+1 < top)
            quickSort(upper + 1, top);
    }

    // ここから始まり、指定する範囲までは変更してはならないコードである。

    public void setTarget(byte [] target) {
        myTarget = target; if(myTarget.length>0) targetReady = true;
    }

    public int frequency() {
        if(targetReady == false) return -1;
        if(spaceReady == false) return 0;
        return subByteFrequency(0, myTarget.length);
        // return slowSubByteFrequency(0, myTarget.length);
    }

    // 今回の演習では，myTargetを含む頭に含むsuffixの数を知りたいというもの．
    public int slowSubByteFrequency(int start, int end){
        int spaceLength = mySpace.length;                      
        int count = 0;                                        
        for(int offset = 0; offset< spaceLength - (end - start - 1); offset++) {
            boolean abort = false; 
            for(int i = 0; i< (end - start); i++) {
                if(myTarget[start+i] != mySpace[offset+i]) {
                    abort = true; 
                    break; 
                }
            }
            if(abort == false) {
                count++;
            }
        }
        return count;
    }

    public int subByteFrequency(int start, int end) {
        // start, and end specify a string to search in myTarget,
        // if myTarget is "ABCD", 
        //     start=0, and end=1 means string "A".
        //     start=1, and end=3 means string "BC".
        // This method returns how many the string appears in my Space.
        // The following the counting method using suffix array.
        int first = subByteStartIndex(start, end);
        int last1 = subByteEndIndex(start, end);
        return last1 - first;
    }
    // 変更してはいけないコードはここまで。

    private int targetCompare(int i, int j, int k) {
        // subByteStartIndexとsubByteEndIndexを定義するときに使う比較関数。
        // 次のように定義せよ。
        // suffix_i is a string starting with the position i in "byte [] mySpace".
        // When mySpace is "ABCD", suffix_0 is "ABCD", suffix_1 is "BCD", 
        // suffix_2 is "CD", and sufffix_3 is "D".
        // target_j_k is a string in myTarget start at j-th postion ending k-th position.
        // if myTarget is "ABCD", 
        //     j=0, and k=1 means that target_j_k is "A".
        //     j=1, and k=3 means that target_j_k is "BC".
        // This method compares suffix_i and target_j_k.
        // if the beginning of suffix_i matches target_j_k, it return 0.
        // if suffix_i > target_j_k it return 1; 
        // if suffix_i < target_j_k it return -1;
        // if first part of suffix_i is equal to target_j_k, it returns 0;
        //
        // Example of search 
        // suffix          target
        // "o"       >     "i"
        // "o"       <     "z"
        // "o"       =     "o"
        // "o"       <     "oo"
        // "Ho"      >     "Hi"
        // "Ho"      <     "Hz"
        // "Ho"      =     "Ho"
        // "Ho"      <     "Ho "   : "Ho " is not in the head of suffix "Ho"
        // "Ho"      =     "H"     : "H" is in the head of suffix "Ho"
        // The behavior is different from suffixCompare on this case.
        // For example,
        //    if suffix_i is "Ho Hi Ho", and target_j_k is "Ho", 
        //            targetCompare should return 0;
        //    if suffix_i is "Ho Hi Ho", and suffix_j is "Ho", 
        //            suffixCompare should return 1. (It was written -1 before 2021/12/21)

        // 1文字ずつ比較
        int i_suf = this.suffixArray[i];
        for(; (i_suf < this.mySpace.length)&&(j < k); i_suf++,j++){
            if (this.mySpace[i_suf] > this.myTarget[j]){
                return 1;
            } else if (this.mySpace[i_suf] < this.myTarget[j]){
                return -1;
            }
        }

        /** 
         * ここまで到達したということは，これまで比較した文字がすべて等しく， 
         * どちらかの文字列が終端まで達してしまったということ．
         * target の方が終端に達していないと， = つまり return 0 にはならないので，
         */
        // ターゲットを全部読み込めなかった場合
        if (j < k){
            return -1;
        } else {
            return 0;
        }
    }

    /**
     * suffix arrayのなかで、目的の文字列の出現が始まる位置を求めるメソッド
     * @param start myTargetのstart
     * @param end myTargetのend
     * @return
     */
    public int subByteStartIndex(int start, int end) {
        //
        /* Example of suffix created from "Hi Ho Hi Ho"
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

        // It returns the index of the first suffix 
        // which is equal or greater than target_start_end.                         
        // Suppose target is set "Ho Ho Ho Ho"
        // if start = 0, and end = 2, target_start_end is "Ho".
        // if start = 0, and end = 3, target_start_end is "Ho ".
        // Assuming the suffix array is created from "Hi Ho Hi Ho",                 
        // if target_start_end is "Ho", it will return 5.                           
        // Assuming the suffix array is created from "Hi Ho Hi Ho",                 
        // if target_start_end is "Ho ", it will return 6.                

        // suffixArray に対する index．
        int c = this.suffixArray.length/2;
        int s = 0;
        int e = this.suffixArray.length;
        int cmp;

        while (!(e-s <= 1)){
            cmp = this.targetCompare(c, start, end);
            if ((cmp == 1) || (cmp == 0)) {
                // cmp が 1 なら，そこが startIndex になることはない
                // よって -cmp
                e = c - cmp + 1;
                // より上側
                // c は 必ず 1 以上移動し， e は 1 移動する可能性が在る．
                // e と c が重なることは無いため，これで問題ない
                c = (s + c)/2;
            } else if (cmp == -1){
                // cmp が -1 なら，そこが startIndex になることはない
                s = c + 1;
                // より下側
                c = (e + c)/2;
            }
        }
        cmp = this.targetCompare(c, start, end);
        return c-(cmp-1)/2;


        /**
         * e = length として考える
         * 最終的に sc e の1通りになる．
         * sc e すなわち e-s==1 になった時点で while を抜ければ良い
         * その後 cmp を実行
         * cmp == 0 の場合， return c
         * cmp == 1 の場合， return c
         * cmp ==-1 の場合， return c+1
         * すなわち return c-(cmp-1)/2 ということになる．ifよりは速い
         * 更新は， s = c-cmp, e = c-cmp+1, c = (s+c)/2 or (e+c)/2 となる
         * 
         */

    }


    /**
     * @param start myTargetのstart
     * @param end myTargetのend
     * @return
     */
    public int subByteEndIndex(int start, int end) {
        // The meaning of start and end is the same as subByteFrequency.
        /* Example of suffix created from "Hi Ho Hi Ho"
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
        // It returns the index of the first suffix 
        // which is greater than target_start_end; (and not equal to target_start_end)
        // Suppose target is set "High_and_Low",
        // if start = 0, and end = 2, target_start_end is "Hi".
        // if start = 1, and end = 2, target_start_end is "i".
        // Assuming the suffix array is created from "Hi Ho Hi Ho",                   
        // if target_start_end is "Ho", it will return 7 for "Hi Ho Hi Ho".  
        // Assuming the suffix array is created from "Hi Ho Hi Ho",          
        // if target_start_end is"i", it will return 9 for "Hi Ho Hi Ho".    
        //                                                                   
        //　ここにコードを記述せよ                                           

        // suffixArray に対する index．
        // int c = (this.suffixArray.length+1)/2;
        // int s = 0;
        // int e = this.suffixArray.length;
        // int cmp;

        // while (e-s > 1){
        //     cmp = this.targetCompare(c-1, start, end);
        //     if (cmp == 1) {
        //         e = c;
        //         c = (s + c + 1)/2;
        //     } else if ((cmp == 0) || (cmp == -1)){
        //         s = c+1;
        //         // s が必ず c+1 へ移動するのに対して， c = (e+c+1)/2 = c+1 となる場合がある
        //         // それを回避するための工夫
        //         c = (e + c)/2 + 1;
        //     }
        // }
        // cmp = this.targetCompare(c-1, start, end);
        // return c-(cmp+1)/2;
        
        int c = (this.suffixArray.length+1)/2;
        int s = 0;
        int e = this.suffixArray.length;
        int cmp;

        while (e-s > 1){
            // System.out.printf("s, c, e : %d, %d, %d\n", s,c,e);
            cmp = this.targetCompare(c-1, start, end);
            if (cmp == 1) {
                e = c;
                c = (s + c + 1)/2;
            } else if ((cmp == 0) || (cmp == -1)){
                s = c;
                // s が必ず +1 移動するのに対して， c = (e+c+1)/2 = c+1 となる場合がある
                // それを回避するための工夫
                c = (e + c + 1)/2;
            }
        }
        cmp = this.targetCompare(c-1, start, end);
        return c-(cmp+1)/2;
        
        /**
         * 初期値 c = (e+1)/2
         * e = length として考える
         * 最終的に s ce の1通りになる．
         * s ce すなわち e-s==1 になった時点で while を抜ければ良い
         * その後 cmp を実行
         * cmp == 0 の場合， return c
         * cmp == 1 の場合， return c-1 
         * cmp ==-1 の場合， return c
         * すなわち return c-(cmp+1)/2 ということになる．ifよりは速い
         * 更新は， s = c+1, e = c, c = (s+c+1)/2 or (e+c+1)/2 となる
         * 注意点として， cmp を計算するときは c-1 とすることである．
         * 
         */
    }

    ////////////////////////////////////////////////////////
    // FrequencyInterfaceをほとんど利用しない手法 start
    ////////////////////////////////////////////////////////

    /** subByteHogeIndexを呼び出す前に必ずこの関数を呼ぶ */
    public void setTargetIndex(int ti){
        targetIndex = ti;
    }
    /** subByteHogeIndexを呼び出す前に必ずこの関数を呼ぶ */
    public void setSpaceOffset(int so){
        spaceOffset = so;
    }

    /**
     * 1文字比較．this.mySpace[i_suf] と this.myTarget[targetIndex] を比較する
     * @param i suffixArray の index
     * @return
     */
    private int targetCompare2(int i) {
        int i_suf = suffixArray[i] + spaceOffset;
        if ((i_suf >= mySpace.length) 
         || (mySpace[i_suf] < myTarget[targetIndex])){
            return -1;
        } else if (mySpace[i_suf] > myTarget[targetIndex]){
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * suffix arrayのなかで、目的文字の出現が始まる位置を求めるメソッド
     * @param ss space_start の略
     * @param se space_end の略
     * @return
     */
    public int subByteStartIndex2(int ss, int se) {
        int c = (ss+se)/2;
        int s = ss;
        int e = se;
        int cmp;
        while (e-s > 1){
            cmp = this.targetCompare2(c);
            if (cmp == -1){
                s = c + 1;
                c = (e + c)/2;
            }else { //else if ((cmp == 1) || (cmp == 0)) {
                e = c - cmp + 1;
                c = (s + c)/2;
            }
        }
        cmp = this.targetCompare2(c);
        return c-(cmp-1)/2;
    }

    /**
     * suffix arrayのなかで、目的文字の出現が終わる位置を求めるメソッド
     * @param ss space_start の略
     * @param se space_end の略
     * @return
     */
    public int subByteEndIndex2(int ss, int se) {
        int c = (ss+se+1)/2;
        int s = ss;
        int e = se;
        int cmp;
        while (e-s > 1){
            cmp = this.targetCompare2(c-1);
            if (cmp == 1) {
                e = c;
                c = (s + c + 1)/2;
            } else { // else if ((cmp == 0) || (cmp==-1)){
                s = c;
                c = (e + c + 1)/2;
            }
        }
        cmp = this.targetCompare2(c-1);
        return c-(cmp+1)/2;
    }


    ////////////////////////////////////////////////////////
    // FrequencyInterfaceをほとんど利用しない手法 end
    ////////////////////////////////////////////////////////
    


    public static void test(String space, String target, int si, int ei){
        Frequencer frequencerObject = new Frequencer();
        frequencerObject.setSpace(space.getBytes());
        frequencerObject.printSuffixArray();
        frequencerObject.setTarget(target.getBytes());
        int start_index = frequencerObject.subByteStartIndex(0, target.length());
        System.out.print("StartIndex = "+ start_index + " ");
        if(start_index == si) {
            System.out.println("OK");
        } else {
            System.out.println("WRONG, true start index is "+si);
        }
        int end_index = frequencerObject.subByteEndIndex(0, target.length());
        System.out.print("EndIndex = "+ end_index + " ");
        if(end_index == ei) {
            System.out.println("OK");
        } else {
            System.out.println("WRONG, true end index is "+ei);
        }
        int true_result = frequencerObject.slowSubByteFrequency(
            0, frequencerObject.myTarget.length
        );
        int result = frequencerObject.frequency();
        System.out.print("Freq = "+ result+" ");
        if(true_result == result) {
            System.out.println("OK");
        } else {
            System.out.println("WRONG, true freq is "+true_result);
        }
    }


    // Suffix Arrayを使ったプログラムのホワイトテストは、
    // privateなメソッドとフィールドをアクセスすることが必要なので、
    // クラスに属するstatic mainに書く方法もある。
    // static mainがあっても、呼びださなければよい。
    // 以下は、自由に変更して実験すること。
    // 注意：標準出力、エラー出力にメッセージを出すことは、
    // static mainからの実行のときだけに許される。
    // 外部からFrequencerを使うときにメッセージを出力してはならない。
    // 教員のテスト実行のときにメッセージがでると、仕様にない動作をするとみなし、
    // 減点の対象である。
    public static void main(String[] args) {
        Frequencer frequencerObject;
        // try { // テストに使うのに推奨するmySpaceの文字は、"ABC", "CBA", "HHH", "Hi Ho Hi Ho".
            frequencerObject = new Frequencer();
            frequencerObject.setSpace("ABC".getBytes());
            // frequencerObject.printSuffixArray();
            frequencerObject = new Frequencer();
            frequencerObject.setSpace("CBA".getBytes());
            // frequencerObject.printSuffixArray();
            frequencerObject = new Frequencer();
            frequencerObject.setSpace("HHH".getBytes());
            // frequencerObject.printSuffixArray();

            // 重要なテスト
            test("Hi Ho Hi Ho", "H", 3, 7);
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
            
            // H のみの場合
            test("HHHH", "HH", 1, 4);
            test("3210321001230123", "0", 0, 4);
           
        // }
        // catch(Exception e) {
        //     System.
        //     System.out.println("STOP");
        // }
    }
}
