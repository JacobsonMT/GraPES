package com.jacobsonmt.mags.server.services;

import com.jacobsonmt.mags.server.entities.Job;
import com.jacobsonmt.mags.server.entities.JobResult;
import com.jacobsonmt.mags.server.exceptions.ResultFileException;
import com.jacobsonmt.mags.server.settings.JobSettings;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JobRunner {

    private final JobSettings jobSettings;

    public JobRunner(JobSettings jobSettings) {
        this.jobSettings = jobSettings;
    }

    /**
     * ###############################
     * #
     * # USE: ./job.sh 123456_test
     * #
     * # Where input/123456_test.fasta is the input
     * #
     * # Will make process/123456_test/job.dat and job.out
     * # where job.dat contains data for plotting and job.out
     * # contains the raw score (to be normalized to get z-score)
     * #
     * # Does this by making 123456_test.sh from templet_files/run.sh
     * #
     * ################################
     */
    public JobResult run(Job job) throws Exception {

        // Create job directory
        Path jobsDirectory = Paths.get(jobSettings.getRootPath());
        Path inputDirectory = jobsDirectory.resolve(jobSettings.getInputPath());
        Path outputDirectory = jobsDirectory.resolve(jobSettings.getOutputPath()).resolve(String.valueOf(job.getId()));
        Files.createDirectories( inputDirectory );

        // Write content to input
        Path fastaFile = inputDirectory.resolve(getInputFileNameFromJob(job));
        try ( BufferedWriter writer = Files.newBufferedWriter( fastaFile, StandardCharsets.UTF_8) ) {
            writer.write( job.recreateFASTA() );
        }

        // Execute script
        String[] commands = {jobSettings.getCommand(), String.valueOf(job.getId())};
        String output = executeCommand( commands, jobsDirectory );
        log.debug( output );

        // Get output from two created files: {output_path}/{jobId}/job.out and {output_path}/{jobId}/job.dat
        FeatureFileCSVMapping features = parseFeatures(outputDirectory
            .resolve(jobSettings.getOutputFeatureFile())
        );

        ScoreFileCSVMapping score = parseModelScore(outputDirectory
            .resolve(jobSettings.getOutputScoreFile()));

        JobResult result = new JobResult();
        result.setZScoreHuman(score.humanScore);
        result.setZScoreYeast(score.yeastScore);

        result.setDiso(features.dis);
        result.setLen(features.len);
        result.setRun(features.run);
        result.setMax(features.max);
        result.setChg(features.chg);
        result.setNet(features.net);
        result.setGvy(features.gvy);
        result.setPip(features.pip);
        result.setTgo(features.tgo);
        result.setMfc(features.mfc);
        result.setSto(features.sto);
        result.setStc(features.stc);
//        result.setSft(features.sft);
        result.setScn(features.scn);
        result.setSbb(features.sbb);
        result.setPol(features.pol);
        result.setRbp(features.rbp);
        result.setSol(features.sol);
        result.setCat(features.cat);
        result.setR(features.r);
        result.setH(features.h);
        result.setK(features.k);
        result.setD(features.d);
        result.setE(features.e);
        result.setS(features.s);
        result.setT(features.t);
        result.setN(features.n);
        result.setQ(features.q);
        result.setC(features.c);
        result.setG(features.g);
        result.setP(features.p);
        result.setA(features.a);
        result.setV(features.v);
        result.setI(features.i);
        result.setL(features.l);
        result.setM(features.m);
        result.setF(features.f);
        result.setY(features.y);
        result.setW(features.w);

        return result;

    }

    private String getInputFileNameFromJob(Job job) {
        return job.getId() + ".fasta";
    }

    /**
     * Ex file:
     *
     * ######
     * nam     dis     len     run     max     chg     net     gvy     pip     tgo     mfc     sto     stc     rbp     sol     cat     R       H       K       D       E       S       T       N       Q       C        G       P       A       V       I       L       M       F       Y       W
     * 35_test 0.211726384364821       307     1       65      0.358306188925081       -8      -1.0400651465798        -1.27   213.683 0       2       12      0.442347        0.7884  0       4.56    2.93    12.05   8.14    11.07   4.88    2.93    4.56    8.14    1.95    3.25    2.93    4.88    3.25    5.21    9.77    3.9     1.95    2.6     0.97
     * ######
     *
     */
    private FeatureFileCSVMapping parseFeatures(Path featureFile) {
        try (Reader reader = Files.newBufferedReader(featureFile)) {
            CsvToBean<FeatureFileCSVMapping> csvToBean = new CsvToBeanBuilder<FeatureFileCSVMapping>(reader)
                .withSeparator('\t')
                .withType(FeatureFileCSVMapping.class)
                .withIgnoreLeadingWhiteSpace(true)
                .build();

            Iterator<FeatureFileCSVMapping> it = csvToBean.iterator();

            // CSV should only have one line, if it has more just take first
            return it.next();

        } catch ( Exception e ) {
            log.error( "Exception while reading results feature file", e );
            throw new ResultFileException( "Server Error: Something went wrong parsing the result feature file", e);
        }
    }

    /**
     * Ex file:
     *
     * ######
     * "hmn.zs","yst.zs"
     * 0.47320298116811,1.90001336353955
     * ######
     *
     */
    private ScoreFileCSVMapping parseModelScore(Path scoreFile) {
        try (Reader reader = Files.newBufferedReader(scoreFile)) {
            CsvToBean<ScoreFileCSVMapping> csvToBean = new CsvToBeanBuilder<ScoreFileCSVMapping>(reader)
                .withSeparator(',')
                .withType(ScoreFileCSVMapping.class)
                .withIgnoreLeadingWhiteSpace(true)
                .build();

            Iterator<ScoreFileCSVMapping> it = csvToBean.iterator();

            // CSV should only have one line, if it has more just take first
            return it.next();

        } catch ( Exception e ) {
            log.error( "Exception while reading results feature file", e );
            throw new ResultFileException( "Server Error: Something went wrong parsing the result feature file", e );
        }
    }

    private static String executeCommand( String[] command, Path path ) {

        StringBuilder output = new StringBuilder();

        try {
            ProcessBuilder builder = new ProcessBuilder( command )
                .directory( path.toFile() );
//            builder.redirectErrorStream();


//            Process p = Runtime.getRuntime().exec( command, null, path.toFile() );
            Process p = builder.start();

            BufferedReader reader = new BufferedReader( new InputStreamReader( p.getInputStream() ) );

            String line = "";
            while ( ( line = reader.readLine() ) != null ) {
                output.append(line).append("\r\n");
                log.debug(line);
            }

            reader = new BufferedReader( new InputStreamReader( p.getErrorStream() ) );

            while ( ( line = reader.readLine() ) != null ) {
                output.append(line).append("\r\n");
                log.debug(line);
            }


            p.waitFor();
//            reader.close();

        } catch ( Exception e ) {
            log.error("Exception while executing job command", e);
        }

        return output.toString();
    }

    @Data
    public static class ScoreFileCSVMapping {

        @CsvBindByName(column = "hmn.zs")
        private Double humanScore;

        @CsvBindByName(column = "yst.zs")
        private Double yeastScore;

    }

    @Data
    public static class FeatureFileCSVMapping {

        @CsvBindByName(column = "score")
        private Double score;

        @CsvBindByName(column = "zScore")
        private Double zScore;

        @CsvBindByName(column = "dis")
        private Double dis;

        @CsvBindByName(column = "len")
        private Integer len;

        @CsvBindByName(column = "run")
        private Integer run;

        @CsvBindByName(column = "max")
        private Integer max;

        @CsvBindByName(column = "chg")
        private Double chg;

        @CsvBindByName(column = "net")
        private Double net;

        @CsvBindByName(column = "gvy")
        private Double gvy;

        @CsvBindByName(column = "pip")
        private Double pip;

        @CsvBindByName(column = "tgo")
        private Double tgo;

        @CsvBindByName(column = "mfc")
        private Double mfc;

        @CsvBindByName(column = "sto")
        private Integer sto;

        @CsvBindByName(column = "stc")
        private Integer stc;

//        @CsvBindByName(column = "sft")
//        private varchar sft;

        @CsvBindByName(column = "scn")
        private Double scn;

        @CsvBindByName(column = "sbb")
        private Double sbb;

        @CsvBindByName(column = "pol")
        private Double pol;

        @CsvBindByName(column = "rbp")
        private Double rbp;

        @CsvBindByName(column = "sol")
        private Double sol;

        @CsvBindByName(column = "cat")
        private Double cat;

        @CsvBindByName(column = "r")
        private Double r;

        @CsvBindByName(column = "h")
        private Double h;

        @CsvBindByName(column = "k")
        private Double k;

        @CsvBindByName(column = "d")
        private Double d;

        @CsvBindByName(column = "e")
        private Double e;

        @CsvBindByName(column = "s")
        private Double s;

        @CsvBindByName(column = "t")
        private Double t;

        @CsvBindByName(column = "n")
        private Double n;

        @CsvBindByName(column = "q")
        private Double q;

        @CsvBindByName(column = "c")
        private Double c;

        @CsvBindByName(column = "g")
        private Double g;

        @CsvBindByName(column = "p")
        private Double p;

        @CsvBindByName(column = "a")
        private Double a;

        @CsvBindByName(column = "v")
        private Double v;

        @CsvBindByName(column = "i")
        private Double i;

        @CsvBindByName(column = "l")
        private Double l;

        @CsvBindByName(column = "m")
        private Double m;

        @CsvBindByName(column = "f")
        private Double f;

        @CsvBindByName(column = "y")
        private Double y;

        @CsvBindByName(column = "w")
        private Double w;

    }
}
