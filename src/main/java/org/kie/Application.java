package org.kie;

import org.jbpm.process.instance.ProcessInstance;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.UserTaskService;
import org.jbpm.services.api.model.ProcessInstanceDesc;
import org.jbpm.services.api.model.VariableDesc;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.query.QueryContext;
import org.kie.internal.query.QueryFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootApplication
@RestController
public class Application implements CommandLineRunner {
    @Autowired
    private ProcessService processService;
    @Autowired
    private RuntimeDataService runtimeDataService;
    @Autowired
    private UserTaskService userTaskService;



    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
//        processService.startProcess("bill_manage-1.0.0-SNAPSHOT", "bill");


    }
    @GetMapping("/evaluation")
    public ResponseEntity<Long> startEvaluation(Principal principal, @RequestParam String employee) throws Exception {
        Long processInstanceId = -1L;
        if ( principal != null ) {
            Map<String, Object> vars = new HashMap<>();
            vars.put("employee", employee);
            processInstanceId = processService.startProcess("Evaluation-1_0_0-SNAPSHOT", "Evaluation.Evaluation", vars);
        }
        return ResponseEntity.ok(processInstanceId);
    }

    @GetMapping("/selfeval")
    public ResponseEntity<Integer> selfEvaluation(Principal principal, @RequestParam String selfeval) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("selfeval", selfeval);
        List<TaskSummary> taskSummaries = runtimeDataService.getTasksAssignedAsPotentialOwner(principal.getName(), new QueryFilter());
        taskSummaries.forEach(s->{
            userTaskService.start(s.getId(), principal.getName());
            userTaskService.complete(s.getId(), principal.getName(), params);
        });
        return ResponseEntity.ok(taskSummaries.size());
    }

    @GetMapping("/hreval")
    public ResponseEntity<Integer> hrEvaluation(Principal principal, @RequestParam String hreval) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("hreval", hreval);
        List<TaskSummary> taskSummaries = runtimeDataService.getTasksAssignedAsPotentialOwner(principal.getName(), new QueryFilter());
        taskSummaries.forEach(s->{
            userTaskService.claim(s.getId(), principal.getName());
            userTaskService.start(s.getId(), principal.getName());
            userTaskService.complete(s.getId(), principal.getName(), params);
        });
        return ResponseEntity.ok(taskSummaries.size());
    }

    @GetMapping("/pmeval")
    public ResponseEntity<Integer> pmEvaluation(Principal principal, @RequestParam String pmeval) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("pmeval", pmeval);
        List<TaskSummary> taskSummaries = runtimeDataService.getTasksAssignedAsPotentialOwner(principal.getName(), new QueryFilter());
        taskSummaries.forEach(s->{
            userTaskService.claim(s.getId(), principal.getName());
            userTaskService.start(s.getId(), principal.getName());
            userTaskService.complete(s.getId(), principal.getName(), params);
        });
        return ResponseEntity.ok(taskSummaries.size());
    }
    @GetMapping("/completed")
    public ResponseEntity<List<Collection<VariableDesc>>> completedEvaluations(Principal principal) throws Exception {
        Collection<ProcessInstanceDesc> processInstances = runtimeDataService.getProcessInstances(Collections.singletonList(ProcessInstance.STATE_COMPLETED), principal.getName(), new QueryContext());
        return ResponseEntity.ok(processInstances.stream()
                .map(pi->{return runtimeDataService.getVariablesCurrentState(pi.getId());})
                .collect(Collectors.toList())
        );
    }
}