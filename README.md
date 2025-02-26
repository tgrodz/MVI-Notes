# MVI with Jetpack Compose


MVI (Model-View-Intent) is an architectural pattern for building UI 
applications, particularly popular in Android development. It follows a 
reactive model where user actions (Intents) are transformed into states 
(Model), which then update the UI (View).

## Core Components of MVI

### Model
- Responsible for managing data and its changes.
- Stores the current state of the application.
- Can interact with repositories, databases, and APIs.

### View
- Displays the current state (State) on the UI.
- Contains no business logic, only renders the provided state.

### Intent
- Defines user actions (e.g., button clicks).
- Sends events to the business logic (Model).

## Workflow
1. **User performs an action (Intent)** → e.g., clicking a button.
2. **Intent is passed to the Model** → e.g., fetching a list of data.
3. **Model updates the State** → e.g., fetched data is stored in State.
4. **View subscribes to State changes** → UI is updated automatically.


