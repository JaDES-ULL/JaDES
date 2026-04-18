package simkit.random;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Factory for creating {@link RandomNumber} instances.
 * Drop-in replacement for {@code simkit.random.RandomNumberFactory} from the
 * original simkit library.
 *
 * <p>The default implementation returned by {@link #getInstance()} is
 * {@link Congruential}, which uses the same LCG algorithm as Java's
 * {@link java.util.Random}.</p>
 *
 * @author JaDES Team (based on the simkit RandomNumberFactory by Kirk Stork / Arnold Buss)
 */
public class RandomNumberFactory {

    protected static final String DEFAULT_CLASS = "simkit.random.MersenneTwister";
    protected static Class<?> defaultClass;
    protected static final Map<String, Class<?>> cache = new HashMap<>();
    protected static final List<String> searchPackages = new ArrayList<>();
    protected static boolean verbose = false;

    static {
        searchPackages.add("simkit.random");
        try {
            defaultClass = Class.forName(DEFAULT_CLASS);
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    protected RandomNumberFactory() {}

    /**
     * Returns a new instance of the default {@link RandomNumber} class using its
     * no-argument constructor — matching simkit's behaviour exactly.
     * For {@link MersenneTwister} this produces a generator with the default seed (4357).
     */
    public static RandomNumber getInstance() {
        try {
            return (RandomNumber) defaultClass.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Cannot instantiate default RandomNumber", e);
        }
    }

    /**
     * Returns a new default-class {@link RandomNumber} with the given seed.
     * Matches simkit's behaviour: creates an instance via reflection then calls
     * {@link RandomNumber#setSeed(long)}.
     */
    public static RandomNumber getInstance(long seed) {
        try {
            RandomNumber rng = (RandomNumber) defaultClass.getDeclaredConstructor().newInstance();
            rng.setSeed(seed);
            return rng;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Cannot instantiate default RandomNumber", e);
        }
    }

    /** Returns a new default-class {@link RandomNumber} seeded with {@code seeds[0]}. */
    public static RandomNumber getInstance(long[] seeds) {
        try {
            RandomNumber rng = (RandomNumber) defaultClass.getDeclaredConstructor().newInstance();
            rng.setSeeds(seeds);
            return rng;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Cannot instantiate default RandomNumber", e);
        }
    }

    /** Returns a new instance of the named {@link RandomNumber} class, seeded with the current time. */
    public static RandomNumber getInstance(String className) {
        return getInstance(className, System.currentTimeMillis());
    }

    /** Returns a new instance of the named {@link RandomNumber} class with the given seed. */
    public static RandomNumber getInstance(String className, long seed) {
        try {
            Class<?> clazz = findClassFor(className);
            RandomNumber rng = (RandomNumber) clazz.getDeclaredConstructor().newInstance();
            rng.setSeed(seed);
            return rng;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Cannot instantiate RandomNumber: " + className, e);
        }
    }

    /** Returns a new instance of the named {@link RandomNumber} class with the given seeds. */
    public static RandomNumber getInstance(String className, long[] seeds) {
        RandomNumber rng = getInstance(className);
        rng.setSeeds(seeds);
        return rng;
    }

    /** Returns a copy of the given {@link RandomNumber} with the same seed. */
    public static RandomNumber getInstance(RandomNumber source) {
        return getInstance(source.getSeed());
    }

    public static void addSearchPackage(String pkg) { searchPackages.add(pkg); }
    public static void removeSearchPackage(String pkg) { searchPackages.remove(pkg); }
    public static String[] getSearchPackages() { return searchPackages.toArray(new String[0]); }
    public static void setVerbose(boolean v) { verbose = v; }
    public static boolean isVerbose() { return verbose; }
    public static Map<String, Class<?>> getCache() { return new HashMap<>(cache); }

    /** Sets the default implementation class by fully-qualified name. */
    public static void setDefaultClass(String className) {
        try {
            defaultClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Cannot find RandomNumber class: " + className, e);
        }
    }

    /** Returns the class for the given name, searching the registered packages. */
    public static Class<?> getClassFor(String name) {
        return findClassFor(name);
    }

    private static Class<?> findClassFor(String name) {
        Class<?> cached = cache.get(name);
        if (cached != null) return cached;
        try {
            Class<?> c = Class.forName(name);
            cache.put(name, c);
            return c;
        } catch (ClassNotFoundException e) { /* try packages */ }
        for (String pkg : searchPackages) {
            try {
                Class<?> c = Class.forName(pkg + "." + name);
                cache.put(name, c);
                return c;
            } catch (ClassNotFoundException e) { /* continue */ }
        }
        throw new RuntimeException("Cannot find RandomNumber class: " + name);
    }
}
