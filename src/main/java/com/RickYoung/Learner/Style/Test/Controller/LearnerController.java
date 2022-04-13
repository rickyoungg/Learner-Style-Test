package com.RickYoung.Learner.Style.Test.Controller;

import com.RickYoung.Learner.Style.Test.Model.Quiz;
import com.RickYoung.Learner.Style.Test.Model.Result;
import com.RickYoung.Learner.Style.Test.Service.LearnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;

@RestController
@RequestMapping("/learn")
public class LearnerController {
    @Autowired
    LearnerService learnerServicel;
    // have 1 post request that accepts the data from the body of the http request
    //
    // post -> body
    // pass it to our service, learn Service

    @PostMapping(value = "/compute", consumes = {"application/json"})
    public String postJson(@RequestBody Quiz submission){
        return learnerServicel.GradeQuiz(submission);
    }


    @PostMapping("/postbody")
    public String postBody(@RequestBody String fullName) {
        return "Hello " + fullName;
    }

}
