package obzcu.re.mcf;

import obzcu.re.mcf.transformers.AbstractTransformer;
import obzcu.re.mcf.transformers.bukkit.Bukkit;
import obzcu.re.mcf.transformers.io.Files;
import obzcu.re.mcf.transformers.reflection.Reflection;
import org.apache.commons.cli.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class Finder
{

    public static void main(String[] args)
    {
        new Finder(args);
    }

    private boolean useCLI = true;

    private void setup(String[] args) throws Throwable
    {

        useCLI = args.length != 0;

        File inputFile;
        if (useCLI)
        {

            CommandLine cmd;
            HelpFormatter help = new HelpFormatter();
            Options options = new Options();

            Option opt = new Option("i", "input", true, "Input jar");
            opt.setRequired(true);
            options.addOption(opt);

            opt = new Option("t", "temp", false, "temp desc");
            opt.setRequired(false);
            options.addOption(opt);

            CommandLineParser parser = new DefaultParser();
            try
            {
                cmd = parser.parse(options, args);
            }
            catch (Throwable t)
            {
                error(t.getMessage() + "\n");
                help.printHelp("java -jar MCF.jar", options);
                return;
            }

            String inputFilePath = cmd.getOptionValue("i");
            inputFile = new File(inputFilePath);

            boolean tmp = cmd.hasOption("temp");

        }
        else
        {

            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            JFileChooser fc = new NativeJFileChooser();
            fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
            fc.addChoosableFileFilter(new FileNameExtensionFilter("*.jar", "jar"));
            fc.addChoosableFileFilter(new FileNameExtensionFilter("*.class", "class"));
            fc.setAcceptAllFileFilterUsed(false);
            int ret = fc.showDialog(null, "Select input file");
            if (ret != 0) return;
            inputFile = fc.getSelectedFile();

        }

        log("Selected file: " + inputFile.getAbsolutePath());

    }

    private Finder(String[] args)
    {
        try
        {

            setup(args);

            List<AbstractTransformer> transformers = new LinkedList<>();
            transformers.add(new Reflection());
            transformers.add(new Files());
            transformers.add(new Bukkit());

            /* Run all at the same time */
            transformers.parallelStream().forEach(AbstractTransformer::doit);

        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
    }

    private final ThreadLocal<SimpleDateFormat> formatter = ThreadLocal.withInitial(
            () -> new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS"));
    private void error(Object obj)
    {
        String time = formatter.get().format(Calendar.getInstance().getTime());
        System.err.println("[MCFv2 " + time + "] " + obj);
    }
    private void log(Object obj)
    {
        String time = formatter.get().format(Calendar.getInstance().getTime());
        System.out.println("[MCFv2 " + time + "] " + obj);
    }

}
