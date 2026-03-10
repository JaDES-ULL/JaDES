package simkit.random;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Factory for creating {@link RandomVariate} instances by class name.
 * Drop-in replacement for {@code simkit.random.RandomVariateFactory} from the
 * original simkit library.
 *
 * <p>The factory searches for a class with the given name in the registered
 * search packages (default: {@code simkit.random}). The lookup preserves the
 * same convention as the original simkit factory, so names like
 * {@code "ConstantVariate"}, {@code "ExponentialVariate"}, etc. continue to work
 * without any code changes in callers.</p>
 *
 * @author JaDES Team (based on the simkit RandomVariateFactory by Kirk Stork / Arnold Buss)
 */
public class RandomVariateFactory {

    protected static final Map<String, Class<?>> cache = new HashMap<>();
    protected static Set<String> searchPackages = new LinkedHashSet<>();
    protected static boolean verbose = false;
    protected static RandomNumber DEFAULT_RNG = RandomNumberFactory.getInstance();

    static {
        searchPackages.add("simkit.random");
    }

    protected RandomVariateFactory() {}

    /**
     * Returns a new instance of the named {@link RandomVariate} class, configured
     * with the given parameters and the default RNG.
     *
     * @param className simple or fully-qualified class name (e.g. {@code "ConstantVariate"})
     * @param parameters distribution parameters
     * @return configured variate instance
     */
    public static RandomVariate getInstance(String className, Object... parameters) {
        return getInstance(className, DEFAULT_RNG, parameters);
    }

    /**
     * Returns a new instance of the named {@link RandomVariate} class, configured
     * with the given parameters and the specified RNG.
     *
     * @param className simple or fully-qualified class name
     * @param rng       the random number generator to use
     * @param parameters distribution parameters
     * @return configured variate instance
     */
    public static RandomVariate getInstance(String className, RandomNumber rng, Object... parameters) {
        Class<?> clazz = findFullyQualifiedNameFor(className);
        if (clazz == null) {
            throw new IllegalArgumentException("Cannot find RandomVariate class: " + className);
        }
        try {
            RandomVariate rv = (RandomVariate) clazz.getDeclaredConstructor().newInstance();
            rv.setRandomNumber(rng != null ? rng : DEFAULT_RNG);
            if (parameters != null && parameters.length > 0) {
                rv.setParameters(parameters);
            }
            return rv;
        } catch (InstantiationException | IllegalAccessException |
                 NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException("Cannot instantiate RandomVariate: " + className, e);
        }
    }

    /**
     * Returns a copy of the given variate (same class, same RNG, same parameters).
     * @param rv the variate to copy
     * @return a new instance with the same configuration
     */
    public static RandomVariate getInstance(RandomVariate rv) {
        return getInstance(rv.getClass().getName(), rv.getRandomNumber(), rv.getParameters());
    }

    /**
     * Finds the {@link Class} for the given name, searching registered packages.
     * Returns {@code null} if not found (does not throw).
     *
     * @param className simple or fully-qualified name
     * @return the class, or {@code null}
     */
    public static Class<?> findFullyQualifiedNameFor(String className) {
        Class<?> cached = cache.get(className);
        if (cached != null) return cached;

        // Try fully qualified name first
        try {
            Class<?> c = Thread.currentThread().getContextClassLoader().loadClass(className);
            if (RandomVariate.class.isAssignableFrom(c)) {
                cache.put(className, c);
                return c;
            }
        } catch (ClassNotFoundException e) { /* try packages */ }

        // Try each registered search package
        for (String pkg : searchPackages) {
            if (verbose) System.out.println("Checking " + pkg + "." + className);
            try {
                Class<?> c = Thread.currentThread().getContextClassLoader()
                        .loadClass(pkg + "." + className);
                if (RandomVariate.class.isAssignableFrom(c)) {
                    cache.put(className, c);
                    return c;
                }
            } catch (ClassNotFoundException e) { /* continue */ }
        }
        return null;
    }

    public static void addSearchPackage(String pkg) { searchPackages.add(pkg); }
    public static void setSearchPackages(Set<String> packages) { searchPackages = new LinkedHashSet<>(packages); }
    public static Set<String> getSearchPackages() { return new LinkedHashSet<>(searchPackages); }
    public static void setVerbose(boolean v) { verbose = v; }
    public static boolean isVerbose() { return verbose; }
    public static Map<String, Class<?>> getCache() { return new HashMap<>(cache); }
    public static void setDefaultRandomNumber(RandomNumber rng) { DEFAULT_RNG = rng; }
    public static RandomNumber getDefaultRandomNumber() { return DEFAULT_RNG; }
}
