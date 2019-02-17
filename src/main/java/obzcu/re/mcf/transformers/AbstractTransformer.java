package obzcu.re.mcf.transformers;

public abstract class AbstractTransformer
{

    public void doit()
    {

        if (!first()) return;
        if (!run()) return;
        last();

    }

    /* prepare, do pre-checks */
    public abstract boolean first();

    /* this does most of the work */
    public abstract boolean run();

    /* finish up? */
    public abstract boolean last();

}
