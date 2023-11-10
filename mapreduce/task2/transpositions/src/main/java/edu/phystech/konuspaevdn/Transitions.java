package edu.phystech.konuspaevdn;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.mapreduce.lib.partition.TotalOrderPartitioner;
import org.apache.hadoop.mapreduce.lib.partition.InputSampler;
import org.apache.hadoop.util.Tool;


import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class Transitions extends Configured implements Tool {

    private static class OrdKey implements WritableComparable<OrdKey> {
        private Integer count;
        private String word;

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }

        public String getWord() {
            return word;
        }

        public void setWord(String word) {
            this.word = word;
        }

        @Override
        public int compareTo(OrdKey o) {
            int res = -1 * this.count.compareTo(o.count);
            if (res == 0) {
                res = this.word.compareTo(o.word);
            }
            return res;
        }

        @Override
        public void write(DataOutput dataOutput) throws IOException {
            dataOutput.writeInt(count);
            dataOutput.writeUTF(word);
        }

        @Override
        public void readFields(DataInput dataInput) throws IOException {
            this.count = dataInput.readInt();
            this.word = dataInput.readUTF();
        }
    }

    public static class PairMapper extends Mapper<LongWritable, Text, Text, Text> {
        private Text init = new Text();
        private Text transition = new Text();
        @Override
        public void map(LongWritable offset, Text line, Context context) throws IOException, InterruptedException {
            String str = line.toString().replaceAll("\\p{Punct}|\\d", " ").toLowerCase();
            String[] list = str.split("\\s+");
            for (String word : list) {
                if (word.length() < 3) continue;
                char[] letters = word.toCharArray();
                Arrays.sort(letters);
                init.set(new String(letters));
                transition.set(word);
                context.write(init, transition);
            }
        }
    }

    public static class CountReducer extends Reducer<Text, Text, OrdKey, Text> {
        private OrdKey key = new OrdKey();
        private Text transitionCount = new Text();
        @Override
        public void reduce(Text init, Iterable<Text> list, Context context) throws IOException, InterruptedException {
            Iterator<Text> it = list.iterator();
            int count = 0;
            Set<String> set = new HashSet<>();
            while (it.hasNext()) {
                ++count;
                set.add(it.next().toString());
            }
            key.setCount(count);
            key.setWord(init.toString());
            transitionCount.set(Integer.toString(set.size()));
            context.write(key, transitionCount);
        }
    }

    public static class SortMapper extends Mapper<OrdKey, Text, OrdKey, Text> {
        @Override
        public void map(OrdKey key, Text value, Context context) throws IOException, InterruptedException {
            context.write(key, value);
        }
    }

    public static class SortReducer extends Reducer<OrdKey, Text, Text, Text> {
        private Text init = new Text();
        private Text wordCount = new Text();
        private Text transitionCount = new Text();
        private Text record = new Text();
        private static  final Text space = new Text("");
        @Override
        public void reduce(OrdKey key, Iterable<Text> tranCount, Context context) throws IOException, InterruptedException {
            init.set(key.getWord());
            wordCount.set(key.getCount().toString());
            transitionCount = tranCount.iterator().next();
            record.set(init.toString() + "\t" + wordCount.toString() + "\t" + transitionCount.toString());
            context.write(record, space);
        }
    }

    @Override
    public int run(String[] strings) throws Exception {
        Path inputPath = new Path(strings[0]);
        Path outputPath = new Path(strings[1]);
        Path midPath = new Path(strings[1] + "_tmp");
        Path partPath = new Path(strings[1] + "_prt");

        Job count = Job.getInstance();
        count.setJobName("count");
        count.setJarByClass(Transitions.class);

        count.setMapperClass(PairMapper.class);
        count.setReducerClass(CountReducer.class);

        count.setInputFormatClass(TextInputFormat.class);
        count.setOutputFormatClass(SequenceFileOutputFormat.class);

        count.setMapOutputKeyClass(Text.class);
        count.setMapOutputValueClass(Text.class);

        count.setOutputKeyClass(OrdKey.class);
        count.setOutputValueClass(Text.class);

        count.setNumReduceTasks(10);

        TextInputFormat.addInputPath(count, inputPath);
        SequenceFileOutputFormat.setOutputPath(count, midPath);

        if (!count.waitForCompletion(true)) {
            return 1;
        }

        Job sort = Job.getInstance();
        sort.setJobName("sort");
        sort.setJarByClass(Transitions.class);

        sort.setMapperClass(SortMapper.class);
        sort.setReducerClass(SortReducer.class);

        sort.setInputFormatClass(SequenceFileInputFormat.class);
        sort.setOutputFormatClass(TextOutputFormat.class);

        sort.setMapOutputKeyClass(OrdKey.class);
        sort.setMapOutputValueClass(Text.class);

        sort.setOutputKeyClass(Text.class);
        sort.setOutputValueClass(Text.class);

        sort.setNumReduceTasks(10);

        SequenceFileInputFormat.addInputPath(sort, midPath);
        TextOutputFormat.setOutputPath(sort, outputPath);

        InputSampler.Sampler<OrdKey, Text> sampler = new InputSampler.RandomSampler<>(0.3, 10000, 10);
        TotalOrderPartitioner.setPartitionFile(sort.getConfiguration(), partPath);
        InputSampler.writePartitionFile(sort, sampler);
        sort.setPartitionerClass(TotalOrderPartitioner.class);


        if (!sort.waitForCompletion(true)) {
            return 2;
        }

        return 0;
    }

    public static void main(String[] args) throws Exception {
        new Transitions().run(args);
    }
}
