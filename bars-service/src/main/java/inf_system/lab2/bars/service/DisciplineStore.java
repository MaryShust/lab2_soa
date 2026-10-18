package inf_system.lab2.bars.service;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@ApplicationScoped
public class DisciplineStore {

    private static final String DEFAULT_DISCIPLINES = "1,2,3";

    private final Set<Long> knownDisciplines;
    private final Map<Long, Set<Long>> disciplineLabWorks = new ConcurrentHashMap<>();

    public DisciplineStore() {
        String configured = System.getProperty("bars.disciplines");
        if (configured == null || configured.isBlank()) {
            configured = System.getenv("BARS_DISCIPLINES");
        }
        if (configured == null || configured.isBlank()) {
            configured = DEFAULT_DISCIPLINES;
        }
        this.knownDisciplines = Arrays.stream(configured.split(","))
                .map(String::trim)
                .filter(token -> !token.isEmpty())
                .map(Long::valueOf)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public boolean add(Long disciplineId, Long labWorkId) {
        Set<Long> labWorks = disciplineLabWorks.computeIfAbsent(disciplineId,
                key -> ConcurrentHashMap.newKeySet());
        return labWorks.add(labWorkId);
    }

    public boolean contains(Long disciplineId, Long labWorkId) {
        Set<Long> labWorks = disciplineLabWorks.get(disciplineId);
        return labWorks != null && labWorks.contains(labWorkId);
    }

    public Set<Long> labWorksOf(Long disciplineId) {
        return Collections.unmodifiableSet(disciplineLabWorks.getOrDefault(disciplineId, Set.of()));
    }

    public boolean disciplineExists(Long disciplineId) {
        return disciplineId != null && knownDisciplines.contains(disciplineId);
    }
}
