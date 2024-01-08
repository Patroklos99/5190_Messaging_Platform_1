import { Component, OnInit } from "@angular/core";
import { UserCredentials } from "../model/user-credentials";
import {LoginFormComponent} from "../login-form/login-form.component";

@Component({
  selector: "app-login-page",
  templateUrl: "./login-page.component.html",
  styleUrls: ["./login-page.component.css"],
  imports: [
    LoginFormComponent
  ],
  standalone: true
})
export class LoginPageComponent implements OnInit {
  constructor() {}

  ngOnInit(): void {}

  onLogin(UserCredentials: UserCredentials) {
    // À faire
  }
}
