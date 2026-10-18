package inf_system.lab2.client.web;

import inf_system.lab2.client.model.LabWork;
import inf_system.lab2.client.model.LabWorkForm;
import inf_system.lab2.client.service.GatewayService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ClientController {

    private final GatewayService gateway;

    public ClientController(GatewayService gateway) {
        this.gateway = gateway;
    }

    @GetMapping("/")
    public String index(@RequestParam(defaultValue = "1") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(required = false) String sort,
                        @RequestParam(required = false) String filter,
                        Model model) {
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);
        model.addAttribute("filter", filter);
        try {
            List<LabWork> labWorks = gateway.getLabWorks(page, size, sort, filter);
            model.addAttribute("labWorks", labWorks);
            model.addAttribute("emptyPage", labWorks.isEmpty());
        } catch (GatewayService.GatewayException ex) {
            model.addAttribute("error", ex.getMessage());
        }
        return "index";
    }

    @GetMapping("/labworks/new")
    public String newForm(Model model) {
        model.addAttribute("form", new LabWorkForm());
        model.addAttribute("mode", "create");
        return "form";
    }

    @GetMapping("/labworks/{id}")
    public String view(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("labWork", gateway.getLabWork(id));
        } catch (GatewayService.GatewayException ex) {
            model.addAttribute("error", ex.getMessage());
            return "error";
        }
        return "view";
    }

    @GetMapping("/labworks/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        try {
            LabWork labWork = gateway.getLabWork(id);
            LabWorkForm form = new LabWorkForm();
            form.setName(labWork.getName());
            if (labWork.getCoordinates() != null) {
                form.setX(labWork.getCoordinates().getX());
                form.setY(labWork.getCoordinates().getY());
            }
            form.setMinimalPoint(labWork.getMinimalPoint());
            form.setPersonalQualitiesMinimum(labWork.getPersonalQualitiesMinimum());
            form.setAveragePoint(labWork.getAveragePoint());
            if (labWork.getDifficulty() != null) {
                form.setDifficulty(labWork.getDifficulty().name());
            }
            if (labWork.getAuthor() != null) {
                form.setAuthorName(labWork.getAuthor().getName());
                form.setAuthorBirthday(labWork.getAuthor().getBirthday());
                form.setAuthorHeight(labWork.getAuthor().getHeight());
                form.setAuthorWeight(labWork.getAuthor().getWeight());
                form.setAuthorPassportID(labWork.getAuthor().getPassportID());
            }
            model.addAttribute("form", form);
            model.addAttribute("id", id);
            model.addAttribute("mode", "edit");
        } catch (GatewayService.GatewayException ex) {
            model.addAttribute("error", ex.getMessage());
            return "error";
        }
        return "form";
    }

    @PostMapping("/labworks")
    public String create(LabWorkForm form, Model model) {
        try {
            gateway.createLabWork(form);
            return "redirect:/";
        } catch (GatewayService.GatewayException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("form", form);
            model.addAttribute("mode", "create");
            return "form";
        }
    }

    @PostMapping("/labworks/{id}")
    public String update(@PathVariable Long id, LabWorkForm form, Model model) {
        try {
            gateway.updateLabWork(id, form);
            return "redirect:/labworks/" + id;
        } catch (GatewayService.GatewayException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("form", form);
            model.addAttribute("id", id);
            model.addAttribute("mode", "edit");
            return "form";
        }
    }

    @PostMapping("/labworks/{id}/delete")
    public String delete(@PathVariable Long id, Model model) {
        try {
            gateway.deleteLabWork(id);
            return "redirect:/";
        } catch (GatewayService.GatewayException ex) {
            model.addAttribute("error", ex.getMessage());
            return "error";
        }
    }

    @GetMapping("/special/min-author")
    public String specialMinAuthor(Model model) {
        try {
            model.addAttribute("labWork", gateway.getMinimalAuthor());
            model.addAttribute("title", "Объект с минимальным автором");
        } catch (GatewayService.GatewayException ex) {
            model.addAttribute("error", ex.getMessage());
            return "error";
        }
        return "view";
    }

    @GetMapping("/special/by-difficulty")
    public String specialByDifficulty(@RequestParam(required = false) String difficulty, Model model) {
        model.addAttribute("difficulty", difficulty);
        if (difficulty != null && !difficulty.isBlank()) {
            try {
                model.addAttribute("count", gateway.countByDifficulty(difficulty));
            } catch (GatewayService.GatewayException ex) {
                model.addAttribute("error", ex.getMessage());
            }
        }
        return "byDifficulty";
    }

    @GetMapping("/special/greater-than")
    public String specialGreaterThan(@RequestParam(required = false) Double value, Model model) {
        model.addAttribute("value", value);
        if (value != null) {
            try {
                model.addAttribute("labWorks", gateway.greaterThanMinimalPoint(value));
            } catch (GatewayService.GatewayException ex) {
                model.addAttribute("error", ex.getMessage());
            }
        }
        return "greaterThan";
    }

    @PostMapping("/bars/discipline/{disciplineId}/labwork/{labWorkId}/add")
    public String addToDiscipline(@PathVariable Long disciplineId, @PathVariable Long labWorkId,
                                  Model model) {
        try {
            LabWork labWork = gateway.addToDiscipline(disciplineId, labWorkId);
            model.addAttribute("labWork", labWork);
            model.addAttribute("message", "Работа добавлена в дисциплину " + disciplineId);
            return "view";
        } catch (GatewayService.GatewayException ex) {
            model.addAttribute("error", ex.getMessage());
            return "error";
        }
    }

    @PostMapping("/bars/labwork/{labWorkId}/increase")
    public String increaseDifficulty(@PathVariable Long labWorkId,
                                     @RequestParam int steps, Model model) {
        try {
            LabWork labWork = gateway.increaseDifficulty(labWorkId, steps);
            model.addAttribute("labWork", labWork);
            model.addAttribute("message", "Сложность повышена");
            return "view";
        } catch (GatewayService.GatewayException ex) {
            model.addAttribute("error", ex.getMessage());
            return "error";
        }
    }
}
