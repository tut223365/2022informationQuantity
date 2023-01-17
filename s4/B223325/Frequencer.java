package s4.B223325;  // ここは、かならず、自分の名前に変えよ。
import java.lang.*;
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

    // この関数は、デバッグに使ってもよい。mainから実行するときにも使ってよい。
    // リポジトリにpushするときには、main以外からは呼ばれないようにせよ。
    private void printSuffixArray() {
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
        // suffixArrayの前処理は、setSpaceで定義せよ。
        mySpace = space; 
        if(mySpace.length>0) spaceReady = true;
        // First, create unsorted suffix array.
        suffixArray = new int[space.length];
        // put all suffixes in suffixArray.
        for(int i = 0; i< space.length; i++) {
            // Please note that each suffix is expressed by one integer. 
            suffixArray[i] = i; 
        }
        //                                            
        // ここに、int suffixArrayをソートするコードを書け。
        // もし、mySpace が"ABC"ならば、
        // suffixArray = { 0, 1, 2} となること求められる。
        // このとき、printSuffixArrayを実行すると
        //   suffixArray[ 0]= 0:ABC
        //   suffixArray[ 1]= 1:BC
        //   suffixArray[ 2]= 2:C
        // のようになるべきである。
        // もし、mySpace が"CBA"ならば
        // suffixArray = { 2, 1, 0} となることが求めらる。
        // このとき、printSuffixArrayを実行すると
        //   suffixArray[ 0]= 2:A
        //   suffixArray[ 1]= 1:BA
        //   suffixArray[ 2]= 0:CBA
        // のようになるべきである。

        // 文字コードの大きさ A < C  
        // 昇順のバブルソート
        int tmp, cmp;
        for (int i = 0; i < this.suffixArray.length-1; i++) {
            for (int j = 1; j < this.suffixArray.length-i; j++){
                cmp = this.suffixCompare(this.suffixArray[j-1], this.suffixArray[j]);
                // 降順なら == -1
                if (cmp == 1) {
                    tmp = this.suffixArray[j-1];
                    this.suffixArray[j-1] = this.suffixArray[j];
                    this.suffixArray[j] = tmp;
                }
            }
        }
    }

    // ここから始まり、指定する範囲までは変更してはならないコードである。

    public void setTarget(byte [] target) {
        myTarget = target; if(myTarget.length>0) targetReady = true;
    }

    public int frequency() {
        if(targetReady == false) return -1;
        if(spaceReady == false) return 0;
        return subByteFrequency(0, myTarget.length);
    }

    // 今回の演習では，myTargetを含む頭に含むsuffixの数を知りたいというもの．
    // targetCompareが，i,j,k を引数に取り，一般的な表現をされているが，実際に targetCompareに渡される引数は i, start, end だと思われる
    public int slowSubByteFrequency(int start, int end){
        int spaceLength = mySpace.length;                      
        int count = 0;                                        
        for(int offset = 0; offset< spaceLength - (end - start - 1); offset++) {
            boolean abort = false; 
            for(int i = 0; i< (end - start - 1); i++) {
                if(myTarget[start+i] != mySpace[offset+i]) {
                    abort = true; 
                    break; 
                }
            }
            if(abort == false) count++;
        }
        return count;
    }

    public int subByteFrequency(int start, int end) {
        // start, and end specify a string to search in myTarget,
        // if myTarget is "ABCD", 
        //     start=0, and end=1 means string "A".
        //     start=1, and end=3 means string "BC".
        // This method returns how many the string appears in my Space.
        // 
        /* This method should be work as follows, but much more efficient.
           int spaceLength = mySpace.length;                      
           int count = 0;                                        
           for(int offset = 0; offset< spaceLength - (end - start); offset++) {
                boolean abort = false; 
                for(int i = 0; i< (end - start); i++) {
                    if(myTarget[start+i] != mySpace[offset+i]) {
                        abort = true; 
                        break; 
                    }
                }
                if(abort == false) count++;
           }
        */
        // The following the counting method using suffix array.
        // 演習の内容は、適切なsubByteStartIndexとsubByteEndIndexを定義することである。
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
        //
        // ここに比較のコードを書け 
        //
        int i_suf = this.suffixArray[i];
        boolean endi = (i_suf < this.mySpace.length);
        boolean endjk = (j < k);
        while(endi && endjk){
            if (this.mySpace[i_suf] > this.myTarget[j]){
                return 1;
            } else if (this.mySpace[i_suf] < this.myTarget[j]){
                return -1;
            }
            endi = (++i_suf < this.mySpace.length);
            endjk = (++j < k);
        }
        if (!endjk){
            return 0;
        } else {
            return -1;
        }
    }

    /**
     * @param start myTargetのstart
     * @param end myTargetのend
     * @return
     */
    private int subByteStartIndex(int start, int end) {
        //suffix arrayのなかで、目的の文字列の出現が始まる位置を求めるメソッド
        // 以下のように定義せよ。
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
        // which is equal or greater than target_start_end.                         
	// Suppose target is set "Ho Ho Ho Ho"
        // if start = 0, and end = 2, target_start_end is "Ho".
        // if start = 0, and end = 3, target_start_end is "Ho ".
        // Assuming the suffix array is created from "Hi Ho Hi Ho",                 
        // if target_start_end is "Ho", it will return 5.                           
        // Assuming the suffix array is created from "Hi Ho Hi Ho",                 
        // if target_start_end is "Ho ", it will return 6.                
        //                                                                          
        // ここにコードを記述せよ。                                                 

        // suffixArray に対する index．
        // start center end             
        int s = 0;
        // subByteEndIndexとの違い
        int c = this.suffixArray.length/2;
        int e = this.suffixArray.length;

        int cmp;
        // System.out.println("StartIndex"+" "+s+" "+e);
        // System.out.println(c);
        // while (!((s == c) || (c == e))){
        while (!(e == c)){
            cmp = this.targetCompare(c, start, end);
            // subByteEndIndexとの違いはこの条件式
            if ((cmp == 1) || (cmp == 0)) {
                e = c;
                c = (s + c)/2;
            } else if (cmp == -1){
                s = c;
                c = (e + c + 1)/2;
            }
            // System.out.println(c);
        }
        return c;
    }


    /**
     * @param start myTargetのstart
     * @param end myTargetのend
     * @return
     */
    private int subByteEndIndex(int start, int end) {
        //suffix arrayのなかで、目的の文字列の出現しなくなる場所を求めるメソッド
        // 以下のように定義せよ。
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
        // start center end      
        int s = 0;
        // subByteStartIndexとの違い
        int c = (this.suffixArray.length+1)/2;
        int e = this.suffixArray.length;

        int cmp;
        // System.out.println("EndIndex"+" "+s+" "+e);
        // System.out.println(c);
        // while (!((s == c) || (c == e))){
        while (!(c == e)){
            cmp = this.targetCompare(c, start, end);
            // subByteStartIndexとの違い
            if (cmp == 1) {
                e = c;
                c = (s + c)/2;
            } else if ((cmp == 0) || (cmp == -1)){
                s = c;
                c = (e + c + 1)/2;
            }
            // System.out.println(c);
        }
        return c;                                         
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
            frequencerObject.printSuffixArray();
            frequencerObject = new Frequencer();
            frequencerObject.setSpace("CBA".getBytes());
            frequencerObject.printSuffixArray();
            frequencerObject = new Frequencer();
            frequencerObject.setSpace("HHH".getBytes());
            frequencerObject.printSuffixArray();

            // 重要なテスト
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
            int start_index = frequencerObject.subByteStartIndex(0, 1);
            System.out.print("StartIndex = "+ start_index + " ");
            if(start_index == 3) {
                System.out.println("OK");
            } else {
                System.out.println("WRONG");
            }
            int end_index = frequencerObject.subByteEndIndex(0, 1);
            System.out.print("EndIndex = "+ end_index + " ");
            if(end_index == 7) {
                System.out.println("OK");
            } else {
                System.out.println("WRONG");
            }
            int result = frequencerObject.frequency();
            System.out.print("Freq = "+ result+" ");
            int true_result = frequencerObject.slowSubByteFrequency(
                0, frequencerObject.myTarget.length
            );
            if(true_result == result) {
                System.out.println("OK");
            } else {
                System.out.println("WRONG "+true_result);
            }

            // H のみの場合
            frequencerObject = new Frequencer();
            frequencerObject.setSpace("H".getBytes());
            frequencerObject.printSuffixArray();
            frequencerObject.setTarget("H".getBytes());
            result = frequencerObject.frequency();
            System.out.print("Freq = "+ result+" ");
            true_result = frequencerObject.slowSubByteFrequency(
                0, frequencerObject.myTarget.length
            );
            if(true_result == result) {
                System.out.println("OK");
            } else {
                System.out.println("WRONG "+true_result);
            }
        // }
        // catch(Exception e) {
        //     System.
        //     System.out.println("STOP");
        // }
    }
}

