Lipa Link — Android Debt Tracking App

A Kotlin/Android app for tracking informal debts between friends, family, and
colleagues — with structured repayment terms and automated email follow-up
reminders. Built as a 4-person group project.

Team & Modules

MemberModuleBranchKellyDebt Management — create, view, edit, deletefeature/debt-managementEmmanuel Kisire (190506)Repayment Terms — capture agreed terms at point of issuefeature/repayment-termsCedricAuth + Navigation + GitHub managementfeature/authBenedict KiruiEmail Follow-Up Reminders — automated email nudgesfeature/email-followup

What the App Does

A lender logs a debt against a borrower (name, email, amount), optionally
attaches structured repayment terms (instalments, frequency, due date), and
can schedule an automated email reminder that fires in the background —
even if the app is closed — nudging the borrower to repay.


App Flow

Login / Auth (Cedric)
        │
        ▼
DebtListFragment  ◄──────────────┐
   │        │        │            │
   │        │        │            │
   ▼        ▼        ▼            │
Add/Edit   Repayment  Email        │
 Debt       Terms     Follow-Up    │
(Kelly)   (Emmanuel) (Benedict)    │
   │        │        │            │
   └────────┴────────┴────────────┘
        (all return to debt list)


Database Schema (shared AppDatabase, Room/SQLite)

users
├── id            INT (primary key)
├── name          TEXT
├── email         TEXT
└── password      TEXT

debts
├── id            INT (primary key, auto-generated)
├── personName    TEXT
├── personEmail   TEXT (default "")
├── amount        DOUBLE
├── dateIssued    LONG (timestamp)
├── notes         TEXT
└── isPaid        BOOLEAN

repayment_terms
├── id                    INT (primary key, auto-generated)
├── debtId                INT (links to debts table)
├── totalAmount           DOUBLE
├── numberOfInstalments   INT
├── instalmentFrequency   TEXT ("Weekly", "Bi-weekly", "Monthly", "Once (lump sum)", "Custom")
├── agreedDate            LONG (timestamp)
├── dueDateFinal          LONG (timestamp)
└── notes                 TEXT (optional)

Database version: 4 (bumped from 3 to add personEmail to debts).
fallbackToDestructiveMigration() is active, so schema changes rebuild
local tables automatically during development — no manual migration scripts
needed for this stage of the project.


Module 1 — Debt Management (Kelly)

Create, view, edit, and delete debts — the core of the app.

Files

FileLocationPurposeDebt.ktdata/Room entity: id, personName, personEmail, amount, dateIssued, notes, isPaidDebtDao.ktdata/insert, update, delete, getAllDebts() (Flow), getDebtById()DebtAdapter.ktroot packageRecyclerView adapter with edit/delete/repayment/remind callbacksDebtListFragment.ktui/debt/Shows all debts, handles delete, navigates to add/edit, repayment, and email screensAddEditDebtFragment.ktui/debt/Single form for creating and editing a debt (debtId = -1 means new)fragment_debt_list.xml, fragment_add_edit_debt.xml, item_debt.xmlres/layout/Layouts

Flow

DebtListFragment <-> AddEditDebtFragment
        -> RepaymentTermsFragment
        -> EmailFollowUpFragment

Fragments call AppDatabase.getDatabase(requireContext()).debtDao() directly
inside lifecycleScope.launch { } — no Repository/ViewModel layer yet
(noted as a possible future improvement, see below).


Module 2 — Repayment Terms (Emmanuel Kisire, 190506)

Captures the agreed repayment plan at the point a debt is issued — how much
is owed, how many instalments, how frequently, and the final due date.

Files

FileLocationPurposeRepaymentTerm.ktdata/Room entity for the repayment terms tableRepaymentTermDao.ktdata/insert, update, query, delete — including getTermsForDebtOnce() for background accessRepaymentTermsFragment.ktui/repayment/Fragment for the repayment terms formfragment_repayment_terms.xmlres/layout/Full form layout

How it works


User taps "Repayment" on a debt in the list
Screen navigates with the debtId
If terms already exist for that debt → pre-fills the form (edit mode)
If not → opens a blank form (create mode)
User fills in amount, instalments, frequency (spinner), due date (date picker), optional notes
Saving writes to the database and returns to the debt list


Key design decisions


Single shared database — avoids runtime conflicts when branches merge
LiveData observation — form always reflects the latest saved state
Date picker instead of free text — due date is always a valid, parseable Long timestamp
Insert with REPLACE strategy — re-saving updates the existing record instead of duplicating it


Dependencies: androidx.room:room-runtime, androidx.room:room-ktx,
androidx.room:room-compiler (via KSP), androidx.lifecycle:lifecycle-viewmodel-ktx,
kotlinx-coroutines-android


Module 3 — Email Follow-Up Reminders (Benedict Kirui)

Lets a user schedule an automated email reminder to a borrower. Sent in the
background via WorkManager, even if the app has been closed.

Files

FileLocationPurposeEmailFollowUpFragment.ktui/email/Screen — loads the debt, shows borrower + terms, checks internet, schedules the reminderfragment_email_followup.xmlres/layout/Layout for the screen aboveReminderScheduler.ktui/email/Packages reminder data and hands it to WorkManager with the chosen delayEmailReminderWorker.ktui/email/Background task — reads data, pulls credentials, queries lender/terms, sends via JavaMail

How a reminder gets sent

User taps "Schedule Email Reminder"
        │
        ▼
EmailFollowUpFragment reads the chosen delay
        │
        ▼
ReminderScheduler packages debtId, name, email, amount
   into a WorkManager OneTimeWorkRequest
        │
        ▼
WorkManager holds the request (survives app close / phone restart)
        │
        ▼
   ...delay elapses...
        │
        ▼
EmailReminderWorker.doWork() runs in the background:
   1. Reads borrower name/email/amount from the request
   2. Reads Gmail sender credentials from BuildConfig
   3. Looks up the lender's name and any repayment terms from the database
   4. Builds the email body
   5. Sends it via JavaMail over Gmail's SMTP server
   6. Returns Result.success() or Result.retry() on failure

Edge case handling


No internet at scheduling time — detected via ConnectivityManager, shows a warning toast, but still schedules; WorkManager sends once reconnected
No borrower email on file — "Schedule Email Reminder" button is disabled, with an explanatory toast
Send failure at delivery time — worker catches the exception and returns Result.retry(), so WorkManager retries automatically



Module 4 — Auth + Navigation + GitHub Management (Cedric)

Handles login/registration and the app's overall navigation graph, and
manages the shared team repository (branches, pull requests, merges).

(Detailed module README not yet available at time of writing — Cedric to
fill in file list and flow if this needs expanding before submission.)


Setup — Credentials

Two secrets are required locally and are not committed to source
control (excluded via .gitignore):


Create local.properties in the project root (or confirm it already
has the Android Studio–generated sdk.dir line), then add:


   gmail.username=lipalink6@gmail.com
   gmail.app.password=<app password, no spaces>


app/build.gradle.kts reads these at build time and exposes them as
BuildConfig.GMAIL_USERNAME / BuildConfig.GMAIL_APP_PASSWORD.
Anyone cloning this repo must supply their own local.properties with
valid Gmail credentials for the email module to send reminders — the
rest of the app runs fine without it.


Requirements


Android Studio (latest stable)
JDK 17 (bundled with Android Studio)
Min SDK: 26 (Android 8.0)
Kotlin, Jetpack (Room, WorkManager, Navigation Component), JavaMail


How to Test — Full App


Run the app on an emulator or physical device
Log in / register (Cedric's auth module)
Add a debt if none exist (tap the + button) — include a borrower email to unlock reminders
Tap Repayment on a debt → fill in terms → save → re-open to confirm it pre-fills (Emmanuel's module)
Tap Remind on the same debt → confirm the terms show in the summary card → schedule a reminder → confirm it sends (Benedict's module)
Tap Edit / Delete on a debt to confirm CRUD still works end-to-end (Kelly's module)


Demo Videos

Due to GitHub's file size limits, functionality is demonstrated across
three shorter clips rather than one large recording:


WithInternet.mp4 — scheduling and sending a reminder with a normal connection
WithOutInternet.mp4 — scheduling a reminder while offline (warning shown, still schedules, sends once reconnected)
NoEmailAddr.mp4 — attempting to schedule a reminder for a debt with no borrower email on file (button disabled, explanatory toast)


(Link each video here once uploaded — e.g. via Google Drive with public
sharing enabled, since GitHub isn't well suited to hosting video directly.)

Known Limitations / Possible Future Work


No Repository/ViewModel layer yet — fragments talk to the database directly
Legacy Activity-based screens (AddDebtActivity, DebtListActivity, EditDebtActivity) still exist alongside the newer Fragment-based navigation and were only updated enough to share the same database — a full cleanup/removal would simplify the codebase
No SQL migration scripts — relies on fallbackToDestructiveMigration(), which wipes local data on schema changes during development
