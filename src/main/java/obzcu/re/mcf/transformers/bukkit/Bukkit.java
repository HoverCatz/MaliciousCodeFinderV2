package obzcu.re.mcf.transformers.bukkit;

import obzcu.re.mcf.transformers.AbstractTransformer;
import org.objectweb.asm.tree.ClassNode;

import java.util.List;

public class Bukkit extends AbstractTransformer
{

    public Bukkit(List<ClassNode> classNodes)
    {
        super(classNodes, Bukkit.class.getName());
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
