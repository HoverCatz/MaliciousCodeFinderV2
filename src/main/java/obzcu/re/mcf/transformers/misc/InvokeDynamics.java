package obzcu.re.mcf.transformers.misc;

import obzcu.re.mcf.transformers.AbstractTransformer;
import org.objectweb.asm.tree.ClassNode;

import java.util.List;

public class InvokeDynamics extends AbstractTransformer
{

    public InvokeDynamics(List<ClassNode> classNodes)
    {
        super(classNodes, InvokeDynamics.class.getName());
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
