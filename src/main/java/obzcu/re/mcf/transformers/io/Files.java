package obzcu.re.mcf.transformers.io;

import obzcu.re.mcf.transformers.AbstractTransformer;
import org.objectweb.asm.tree.ClassNode;

import java.util.List;

public class Files extends AbstractTransformer
{

    public Files(List<ClassNode> classNodes)
    {
        super(classNodes, Files.class.getName());
    }

    @Override
    public boolean first()
    {
        return true;
    }

    @Override
    public boolean run()
    {

        return false;
    }

    @Override
    public boolean last()
    {
        return false;
    }

}

