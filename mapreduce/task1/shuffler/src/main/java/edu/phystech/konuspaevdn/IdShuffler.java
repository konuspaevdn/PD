package edu.phystech.konuspaevdn;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;

import java.io.IOException;
import java.util.Iterator;
import java.util.Random;

public class IdShuffler extends Configured implements Tool {
    public static class IdMapper extends Mapper<LongWritable, Text, IntWritable, Text> {
        private Random random = new Random();
        private IntWritable num = new IntWritable();

        public void map(LongWritable offset, Text id, Context context) throws IOException, InterruptedException {
            num.set(random.nextInt(25));
            context.write(num, id);
        }
    }

    public static class IdReducer extends Reducer<IntWritable, Text, Text, Text> {

        private final static Text space = new Text("");
        private Random random = new Random();
        private Text line = new Text();
        public void reduce(IntWritable num, Iterable<Text> ids, Context context) throws IOException, InterruptedException {
            Iterator<Text> it = ids.iterator();
            String str = "";
            str += it.next().toString();
            int size = random.nextInt(5) + 1;
            --size;
            while (size > 0 && it.hasNext()) {
                str += "," + it.next().toString();
                --size;
            }
            line.set(str);
            context.write(space, line);
        }
    }

    @Override
    public int run(String[] strings) throws Exception {
        Path inputPath = new Path(strings[0]);
        Path outputPath = new Path(strings[1]);

        Job job1 = Job.getInstance();
        job1.setJarByClass(IdShuffler.class);

        job1.setMapperClass(IdMapper.class);
        job1.setReducerClass(IdReducer.class);

        job1.setInputFormatClass(TextInputFormat.class);
        job1.setOutputFormatClass(TextOutputFormat.class);

        job1.setMapOutputKeyClass(IntWritable.class);
        job1.setMapOutputValueClass(Text.class);

        job1.setOutputKeyClass(Text.class);
        job1.setOutputValueClass(Text.class);

        job1.setNumReduceTasks(5);

        TextInputFormat.addInputPath(job1, inputPath);
        TextOutputFormat.setOutputPath(job1, outputPath);

        return job1.waitForCompletion(true)? 0: 1;
    }

    public static void main(String[] args) throws Exception {
        new IdShuffler().run(args);
    }

}
