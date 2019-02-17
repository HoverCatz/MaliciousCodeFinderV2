package obzcu.re.mcf.transformers.reflection;

import obzcu.re.mcf.transformers.AbstractTransformer;
import org.objectweb.asm.tree.ClassNode;

import java.util.List;

public class Reflection extends AbstractTransformer
{

    public Reflection(List<ClassNode> classNodes)
    {
        super(classNodes, Reflection.class.getName());
    }

    @Override
    public boolean first()
    {
        return true;
    }

    @Override
    public boolean run()
    {

        for (ClassNode node : classNodes)
        {
            log(node.name + " - " + node.superName);
        }

        return false;
    }

    @Override
    public boolean last()
    {
        return false;
    }

}
