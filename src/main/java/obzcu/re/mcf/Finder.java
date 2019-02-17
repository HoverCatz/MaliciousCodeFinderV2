package obzcu.re.mcf;

import obzcu.re.mcf.transformers.AbstractTransformer;
import obzcu.re.mcf.transformers.bukkit.Bukkit;
import obzcu.re.mcf.transformers.io.Files;
import obzcu.re.mcf.transformers.reflection.Reflection;
import org.apache.commons.cli.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Finder
{

    public static void main(String[] args)
    {
        new Finder(args);
    }

    private static StringBuffer guiOutputText = new StringBuffer(),
                                outputText = new StringBuffer();
    private static File inputFile, logFile = null;

    public static boolean   useCLI = true,
                            forcedLog = false,
                            oct = false,
                            compact = false,
                            xcompact = false;

    private int setup(String[] args) throws Throwable
    {

        useCLI = args.length != 0;

        if (useCLI)
        {

            CommandLine cmd;
            HelpFormatter help = new HelpFormatter();
            Options options = new Options();

            Option opt = new Option("i", "input", true, "Input jar");
            opt.setRequired(true);
            options.addOption(opt);

            opt = new Option("o", "output", true, "Output log file - will replace console output");
            opt.setRequired(false);
            options.addOption(opt);

            opt = new Option("f", "force", false, "if logfile already exists, overwrite it instead of exiting");
            opt.setRequired(false);
            options.addOption(opt);

            opt = new Option("oct", "consoletoo", false, "Only if you use -o, but want both console & file output");
            opt.setRequired(false);
            options.addOption(opt);

            opt = new Option("c", "compact", false, "show compact result - not compatible with -x");
            opt.setRequired(false);
            options.addOption(opt);

            opt = new Option("x", "xcompact", false, "return results in one line - not compatible with -c");
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
                return -1;
            }

            inputFile = new File(cmd.getOptionValue("i"));

            if (cmd.hasOption("f"))
                forcedLog = true;

            if (cmd.hasOption("o"))
            {
                logFile = new File(cmd.getOptionValue("o"));
                if (logFile.exists() && !forcedLog)
                {
                    throw new FileAlreadyExistsException("Log file already exists: " + logFile.getAbsolutePath());
                }
            }

            if (cmd.hasOption("oct"))
                oct = true;

            boolean c = cmd.hasOption("c");
            boolean x = cmd.hasOption("x");

            if (c && x)
            {
                throw new IllegalStateException("You can only use either -c or -x, not both at the same time.");
            }

            if (c)
                compact = true;

            if (x)
                xcompact = true;

        }
        else
        {

            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            JFileChooser fc = new NativeJFileChooser();
            fc.setCurrentDirectory(new File(System.getProperty("user.home") + "/Desktop"));
            fc.addChoosableFileFilter(new FileNameExtensionFilter("*.jar", "jar"));
            fc.addChoosableFileFilter(new FileNameExtensionFilter("*.class", "class"));
            fc.setAcceptAllFileFilterUsed(false);
            int ret = fc.showDialog(null, "Select input file");
            if (ret != 0) return -2;
            inputFile = fc.getSelectedFile();

        }

        if (!inputFile.exists())
        {
            throw new FileNotFoundException("Input file not found: " + inputFile.getAbsolutePath());
        }

        log("Selected file: " + inputFile.getAbsolutePath());

        return 0;

    }

    private Finder(String[] args)
    {
        try
        {

            int ret = setup(args);
            if (ret == -1)
            {
                return;
            }
            else
            if (ret == -2)
            {
                if (useCLI)
                    log("Operation canceled.");
                else
                    useCLI = true;
                return;
            }
            else
            if (ret != 0)
            {
                throw new IllegalStateException("Unknown return value " + ret + " from setup.");
            }

            /* Add all class files to list */
            List<ClassNode> classNodes = new CopyOnWriteArrayList<>();
            if (inputFile.getName().endsWith(".jar"))
            {
                JarFile jar = new JarFile(inputFile);
                Enumeration<? extends JarEntry> entries = jar.entries();
                while (entries.hasMoreElements())
                {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();
                    if (name.endsWith(".class"))
                    {
                        byte[] bytes = readFully(jar.getInputStream(entry));
                        ClassReader reader = new ClassReader(bytes);
                        ClassNode node = new ClassNode();
                        reader.accept(node, ClassReader.EXPAND_FRAMES);
                        classNodes.add(node);
                    }
                }
                jar.close();
            }
            else
            if (inputFile.getName().endsWith(".class"))
            {
                byte[] bytes = readFully(new FileInputStream(inputFile));
                ClassReader reader = new ClassReader(bytes);
                ClassNode node = new ClassNode();
                reader.accept(node, ClassReader.EXPAND_FRAMES);
                classNodes.add(node);
            }
            else
            {
                throw new IllegalArgumentException("Invalid file format. Only .jar and .class supported.");
            }

            /* Add all transformers */
            List<AbstractTransformer> transformers = new LinkedList<>();
            transformers.add(new Reflection(classNodes));
            transformers.add(new Files(classNodes));
            transformers.add(new Bukkit(classNodes));

            /* Run all transformers */
            transformers.parallelStream().forEach(AbstractTransformer::doit);

            if (!compact && !xcompact)
            {
                for (AbstractTransformer at : transformers)
                {
                    outputText.append(at.detailed.toString());
                }
            }
            else
            if (compact)
            {
                for (AbstractTransformer at : transformers)
                {
                    outputText.append(at.detailed.toString());
                }
            }
            else
            // xcompact
            {
                for (AbstractTransformer at : transformers)
                {
                    outputText.append(at.output.toString());
                }
            }

        }
        catch (Throwable t)
        {
            if (xcompact)
            {
                String err = t.getMessage();
                if (t.getCause() != null)
                    err += " | " + t.getCause().getMessage();
                error(err);
            }
            else
            {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                t.printStackTrace(pw);
                error(sw.toString());
            }
        }
        finally
        {

            if (logFile != null)
            {
                try
                {
                    if (logFile.exists())
                    {
                        boolean delete = logFile.delete();
                        if (!delete) throw new IOException("Couldn't delete log file '" + logFile.getAbsolutePath() + "'.");
                    }
                    boolean newFile = logFile.createNewFile();
                    if (!newFile) throw new IOException("Couldn't create new file '" + logFile.getAbsolutePath() + "'.");
                    java.nio.file.Files.write(logFile.toPath(), outputText.toString().getBytes(StandardCharsets.UTF_8));
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }

            /* If gui (f.ex by direct run), show output in dialog box */
            if (!useCLI)
            {
                String output = guiOutputText.toString();
                JOptionPane.showMessageDialog(null, output.trim().isEmpty() ? "No output." : output);
            }

        }
    }

    private byte[] readFully(InputStream is) throws Throwable
    {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        while ((nRead = is.read(data, 0, data.length)) != -1)
            buffer.write(data, 0, nRead);
        buffer.flush();
        is.close();
        return buffer.toByteArray();
    }

    private static final ThreadLocal<SimpleDateFormat> formatter = ThreadLocal.withInitial(
            () -> new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS"));
    public static void error(Object obj)
    {
        String time = formatter.get().format(Calendar.getInstance().getTime());
        log("[MCFv2 " + time + "] " + obj, true);
    }
    public static void log(Object obj)
    {
        String time = formatter.get().format(Calendar.getInstance().getTime());
        log("[MCFv2 " + time + "] " + obj, false);
    }
    private static void log(Object obj, boolean error)
    {
        if (!useCLI || oct)
            guiOutputText.append(obj.toString().replace("\t", "        ")).append('\n');
        outputText.append(obj).append(xcompact ? '|' : '\n');
        if (useCLI || oct)
            if (error) System.err.print(obj + (xcompact ? " | " : "\n"));
            else System.out.print(obj + (xcompact ? " | " : "\n"));
    }

}
