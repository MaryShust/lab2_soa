package inf_system.lab2.bars.service;

import inf_system.lab2.bars.client.LabWorkClient;
import inf_system.lab2.bars.model.Difficulty;
import inf_system.lab2.bars.model.LabWork;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Optional;

@ApplicationScoped
public class BarsService {

    private static final Difficulty[] SCALE = {Difficulty.NORMAL, Difficulty.INSANE, Difficulty.TERRIBLE};

    @Inject
    private DisciplineStore disciplineStore;

    @Inject
    private LabWorkClient labWorkClient;

    public LabWork addLabWorkToDiscipline(Long disciplineId, Long labWorkId) {
        if (disciplineId == null || disciplineId < 1 || labWorkId == null || labWorkId < 1) {
            throw new BadRequestException("Некорректные идентификаторы");
        }

        if (!disciplineStore.disciplineExists(disciplineId)) {
            throw new NotFoundException("Лабораторная работа или дисциплина не найдена");
        }

        Optional<LabWork> labWork = labWorkClient.getById(labWorkId);
        if (labWork.isEmpty()) {
            throw new NotFoundException("Лабораторная работа или дисциплина не найдена");
        }

        boolean added = disciplineStore.add(disciplineId, labWorkId);
        if (!added) {
            throw new ConflictException("Работа уже добавлена в программу дисциплины");
        }
        return labWork.get();
    }

    public LabWork increaseDifficulty(Long labWorkId, Integer stepsCount) {
        if (labWorkId == null || labWorkId < 1) {
            throw new BadRequestException("Некорректный шаг или превышение максимальной сложности");
        }
        if (stepsCount == null || stepsCount < 1) {
            throw new BadRequestException("Некорректный шаг или превышение максимальной сложности");
        }

        LabWork labWork = labWorkClient.getById(labWorkId)
                .orElseThrow(() -> new NotFoundException("Лабораторная работа не найдена"));

        Difficulty current = labWork.getDifficulty();
        if (current == null) {
            throw new BadRequestException("Некорректный шаг или превышение максимальной сложности");
        }
        int index = current.ordinal() + stepsCount;
        if (index >= SCALE.length) {
            throw new BadRequestException("Некорректный шаг или превышение максимальной сложности");
        }
        labWork.setDifficulty(SCALE[index]);
        return labWorkClient.update(labWork);
    }

    public long countByDifficulty(String difficulty) {
        return labWorkClient.countByDifficulty(difficulty);
    }

    public static class BadRequestException extends RuntimeException {
        public BadRequestException(String message) {
            super(message);
        }
    }

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) {
            super(message);
        }
    }

    public static class ConflictException extends RuntimeException {
        public ConflictException(String message) {
            super(message);
        }
    }
}
