package obzcu.re.mcf.transformers.misc;

import obzcu.re.mcf.transformers.AbstractTransformer;

public class InvokeDynamics extends AbstractTransformer
{

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
