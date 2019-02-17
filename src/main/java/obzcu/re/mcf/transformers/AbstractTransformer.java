package obzcu.re.mcf.transformers;

import obzcu.re.mcf.Finder;
import org.objectweb.asm.tree.ClassNode;

import java.util.List;

public abstract class AbstractTransformer
{

    protected final List<ClassNode> classNodes;
    private final String name;
    public final StringBuilder output, detailed;

    public AbstractTransformer(List<ClassNode> classNodes, String name)
    {
        this.classNodes = classNodes;
        this.name = name;
        this.output = new StringBuilder();
        this.detailed = new StringBuilder();
    }

    public void doit()
    {

        if (!first()) return;
        if (!run()) return;
        if (!last()) throw new RuntimeException("Something went wrong inside '" + name + "'.");

    }

    /* prepare, do pre-checks */
    public abstract boolean first();

    /* this does most of the work */
    public abstract boolean run();

    /* finish up? */
    public abstract boolean last();

    public void error(Object obj)
    {
        obj = "[" + name + "] " + obj;
        if (Finder.compact || Finder.xcompact)
            output.append(obj).append(Finder.xcompact ? '|' : '\n');
        else
            detailed.append(obj).append('\n');
        Finder.error(obj);
    }

    public void log(Object obj)
    {
        obj = "[" + name + "] " + obj;
        if (Finder.compact || Finder.xcompact)
            output.append(obj).append(Finder.xcompact ? '|' : '\n');
        else
            detailed.append(obj).append('\n');
        Finder.log(obj);
    }

}
