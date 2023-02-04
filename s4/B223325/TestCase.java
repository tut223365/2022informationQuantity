package s4.B223325; // Please modify to s4.Bnnnnnn, where nnnnnn is your student ID. 
import java.lang.*;
import s4.specification.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

/*
interface FrequencerInterface {     // This interface provides the design for frequency counter.
    void setTarget(byte[]  target); // set the data to search.
    void setSpace(byte[]  space);  // set the data to be searched target from.
    int frequency(); //It return -1, when TARGET is not set or TARGET's length is zero
                    //Otherwise, it return 0, when SPACE is not set or Space's length is zero
                    //Otherwise, get the frequency of TAGET in SPACE
    int subByteFrequency(int start, int end);
    // get the frequency of subByte of taget, i.e target[start], taget[start+1], ... , target[end-1].
    // For the incorrect value of START or END, the behavior is undefined.
}
*/

/*
package s4.specification;
public interface InformationEstimatorInterface{
    void setTarget(byte target[]); // set the data for computing the information quantities
    void setSpace(byte space[]); // set data for sample space to computer probability
    double estimation(); // It returns 0.0 when the target is not set or Target's length is zero;
// It returns Double.MAX_VALUE, when the true value is infinite, or space is not set.
// The behavior is undefined, if the true value is finete but larger than Double.MAX_VALUE.
// Note that this happens only when the space is unreasonably large. We will encounter other problem anyway.
// Otherwise, estimation of information quantity, 
}                        
*/


public class TestCase {
    static boolean success = true;

    static String test(String space, String target, int ans){
        FrequencerInterface  myObject;
        int freq;
        myObject = new Frequencer();
        myObject.setSpace(space.getBytes());
        myObject.setTarget(target.getBytes());
        freq = myObject.frequency();
        if (ans!=freq){
            return space+", "+target+": "+freq;
        }
        return null;
    }

    public static String readAll(final String path) throws IOException {
        return Files.lines(Paths.get(path), Charset.forName("UTF-8"))
            .collect(Collectors.joining(System.getProperty("line.separator")));
    }

    public static void test_data(String s, String t){
        try{
            String space = readAll("../data/"+s);
            String target = readAll("../data/"+t);
            InformationEstimatorInterface myObject = new InformationEstimator();
            long start_time = System.nanoTime();
            myObject.setSpace(space.getBytes());
            myObject.setTarget(target.getBytes());
            myObject.estimation();
            long end_time = System.nanoTime();
            System.out.printf(
                "Benchmarking...(%s, %s) => Success: %f[ms] \n",
                s, t, (double)(end_time-start_time)/1000000.0
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sort_data(String s){
        try{
            String space = readAll("../data/"+s);
            InformationEstimatorInterface myObject = new InformationEstimator();
            long start_time = System.nanoTime();
            myObject.setSpace(space.getBytes());
            long end_time = System.nanoTime();
            System.out.printf(
                "Benchmarking...(%s) => Success: %f[ms] \n",
                s, (double)(end_time-start_time)/1000000.0
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            FrequencerInterface  myObject;
            int freq;
            System.out.println("checking Frequencer");

            // This is smoke test
            myObject = new Frequencer();
            myObject.setSpace("Hi Ho Hi Ho".getBytes());
            myObject.setTarget("H".getBytes());
            freq = myObject.frequency();
            assert freq == 4: "Hi Ho Hi Ho, H: " + freq;
            
            // Write your testCase here
            String result;
            // 「調査配列に対して segmentation error を起こさないようにする変更」に対するテスト
            result = test(
                "Hi Ho Hi HoH",
                "HH",
                0
            );
            assert result == null : result;

            result = test(
                "HHH HHH HHHH",
                "HH",
                7
            );
            assert result == null : result;

        }
        catch(Exception e) {
            System.out.println("Exception occurred in Frequencer Object");
            success = false;
        }

        try {
            InformationEstimatorInterface myObject;
            double value;
            System.out.println("checking InformationEstimator");
            System.out.println("basic test");
            myObject = new InformationEstimator();
            myObject.setSpace("3210321001230123".getBytes());
            myObject.setTarget("0".getBytes());
            value = myObject.estimation();
            assert (value > 1.9999) && (2.0001 >value): "IQ for 0 in 3210321001230123 should be 2.0. But it returns "+value;
            myObject.setTarget("01".getBytes());
            value = myObject.estimation();
            assert (value > 2.9999) && (3.0001 >value): "IQ for 01 in 3210321001230123 should be 3.0. But it returns "+value;
            myObject.setTarget("0123".getBytes());
            value = myObject.estimation();
            assert (value > 2.9999) && (3.0001 >value): "IQ for 0123 in 3210321001230123 should be 3.0. But it returns "+value;
            myObject.setTarget("00".getBytes());
            value = myObject.estimation();
            assert (value > 3.9999) && (4.0001 >value): "IQ for 00 in 3210321001230123 should be 3.0. But it returns "+value;

            // spec test
            System.out.println("spec test");
            myObject = new InformationEstimator();
            // なにも set していない場合
            value = myObject.estimation();
            assert value == 0: "IQ for no_target in no_space should be 0 But it returns "+value;
            // 長さ 0 の target を set した場合
            myObject.setTarget("".getBytes());
            value = myObject.estimation();
            assert value == 0: "IQ for no_target in no_space should be 0 But it returns "+value;
            // space のみ set してない場合
            myObject.setTarget("1".getBytes());
            value = myObject.estimation();
            assert value == Double.MAX_VALUE: "IQ for no_target in no_space should be "+Double.MAX_VALUE+" But it returns "+value;
            // 長さ 0 の space を set した場合
            myObject.setSpace("".getBytes());
            value = myObject.estimation();
            assert value == Double.MAX_VALUE: "IQ for no_target in no_space should be "+Double.MAX_VALUE+" But it returns "+value;
            // どちらも正しく set してあるが，情報量が無限大となる場合
            myObject.setSpace("0123456".getBytes());
            myObject.setTarget("789".getBytes());
            value = myObject.estimation();
            assert value == Double.MAX_VALUE: "IQ for no_target in no_space should be "+Double.MAX_VALUE+" But it returns "+value;

            // data にあるやつで検証
            System.out.println("test_data");
            test_data("space_100b.txt", "target_10b.txt");
            test_data("rand_1k.txt", "target_10b.txt");
            test_data("rand_1k.txt", "target_16b.txt");
            test_data("rand_10k.txt", "target_16b.txt");
            test_data("rand_100k.txt", "target_16b.txt");
            test_data("space_100k.txt", "target_16b.txt");
            test_data("space_100k.txt", "target_1k.txt");
            test_data("space_100k.txt", "target_10k.txt");

            System.out.println("sort_data");
            sort_data("space_100b.txt");
            sort_data("rand_1k.txt");
            sort_data("rand_10k.txt");
            sort_data("rand_100k.txt");
            sort_data("space_100k.txt");
            
        }
        catch(Exception e) {
            System.out.println("Exception occurred in InformationEstimator Object");
            success = false;
        }
        if(success) { System.out.println("TestCase OK"); } 
    }
}        
        
// Benchmarking...(space_100b.txt, target_10b.txt) => Success: 0.491635[ms] 
// Benchmarking...(rand_1k.txt, target_10b.txt) => Success: 1.341883[ms] 
// Benchmarking...(rand_1k.txt, target_16b.txt) => Success: 1.004966[ms] 
// Benchmarking...(rand_10k.txt, target_16b.txt) => Success: 51.544383[ms] 
// Benchmarking...(rand_100k.txt, target_16b.txt) => Success: 4816.874727[ms] 
// Benchmarking...(space_100k.txt, target_16b.txt) => Success: 34841.932298[ms] 
// Benchmarking...(space_100k.txt, target_1k.txt) => Success: 38859.673035[ms] 
// sort_data
// Benchmarking...(space_100b.txt) => Success: 0.086900[ms] 
// Benchmarking...(rand_1k.txt) => Success: 0.764398[ms] 
// Benchmarking...(rand_10k.txt) => Success: 51.256332[ms] 
// Benchmarking...(rand_100k.txt) => Success: 4818.588008[ms] 
// Benchmarking...(space_100k.txt) => Success: 35242.773298[ms] 
// TestCase OK

// Benchmarking...(space_100b.txt, target_10b.txt) => Success: 0.231561[ms] 
// Benchmarking...(rand_1k.txt, target_10b.txt) => Success: 0.848377[ms] 
// Benchmarking...(rand_1k.txt, target_16b.txt) => Success: 0.486206[ms] 
// Benchmarking...(rand_10k.txt, target_16b.txt) => Success: 4.246311[ms] 
// Benchmarking...(rand_100k.txt, target_16b.txt) => Success: 46.831638[ms] 
// Benchmarking...(space_100k.txt, target_16b.txt) => Success: 13783.460147[ms] 
// Benchmarking...(space_100k.txt, target_1k.txt) => Success: 18116.057129[ms] 
// Benchmarking...(rand_1k.txt, target_10k.txt) => Success: 6.707455[ms] 
// sort_data
// Benchmarking...(space_100b.txt) => Success: 0.050352[ms] 
// Benchmarking...(rand_1k.txt) => Success: 0.232573[ms] 
// Benchmarking...(rand_10k.txt) => Success: 3.170558[ms] 
// Benchmarking...(rand_100k.txt) => Success: 42.185552[ms] 
// Benchmarking...(space_100k.txt) => Success: 15441.695380[ms] 