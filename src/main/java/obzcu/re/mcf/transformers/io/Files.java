package obzcu.re.mcf.transformers.io;

import obzcu.re.mcf.transformers.AbstractTransformer;

public class Files extends AbstractTransformer
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

