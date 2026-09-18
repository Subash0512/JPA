/* =========================================================
   HOSPITAL MANAGEMENT SYSTEM
   API SERVICE
   ========================================================= */

const API_BASE_URL = "";


/* =========================================================
   TOKEN
   ========================================================= */

function getToken() {
    return localStorage.getItem("token");
}


/* =========================================================
   COMMON REQUEST
   ========================================================= */

async function apiRequest(endpoint, options = {}) {

    const token = getToken();

    const headers = {
        "Content-Type": "application/json",
        ...options.headers
    };

    if (token) {
        headers["Authorization"] =
            "Bearer " + token;
    }

    try {

        const response = await fetch(
            API_BASE_URL + endpoint,
            {
                ...options,
                headers: headers
            }
        );


        // =====================================================
        // UNAUTHORIZED
        // =====================================================

        if (response.status === 401) {

            console.error(
                "Unauthorized request"
            );

            localStorage.removeItem("token");
            localStorage.removeItem("username");
            localStorage.removeItem("role");

            window.location.href =
                "/login";

            return null;
        }


        // =====================================================
        // FORBIDDEN
        // =====================================================

        if (response.status === 403) {

            console.error(
                "Access denied"
            );

            throw new Error(
                "You do not have permission to perform this action."
            );
        }


        // =====================================================
        // NO CONTENT
        // =====================================================

        if (response.status === 204) {
            return null;
        }


        // =====================================================
        // RESPONSE DATA
        // =====================================================

        const contentType =
            response.headers.get(
                "content-type"
            );

        let data;

        if (
            contentType &&
            contentType.includes(
                "application/json"
            )
        ) {

            data = await response.json();

        } else {

            data = await response.text();
        }


        // =====================================================
        // OTHER HTTP ERRORS
        // =====================================================

        if (!response.ok) {

            const message =
                typeof data === "object" &&
                data?.message
                    ? data.message
                    : "Request failed";

            throw new Error(message);
        }

        return data;

    } catch (error) {

        console.error(
            "API Error:",
            error
        );

        throw error;
    }
}


/* =========================================================
   GET
   ========================================================= */

async function apiGet(endpoint) {

    return apiRequest(
        endpoint,
        {
            method: "GET"
        }
    );
}


/* =========================================================
   POST
   ========================================================= */

async function apiPost(
    endpoint,
    data
) {

    return apiRequest(
        endpoint,
        {
            method: "POST",
            body: JSON.stringify(data)
        }
    );
}


/* =========================================================
   PUT
   ========================================================= */

async function apiPut(
    endpoint,
    data
) {

    return apiRequest(
        endpoint,
        {
            method: "PUT",
            body: JSON.stringify(data)
        }
    );
}


/* =========================================================
   DELETE
   ========================================================= */

async function apiDelete(endpoint) {

    return apiRequest(
        endpoint,
        {
            method: "DELETE"
        }
    );
}


/* =========================================================
   AUTHENTICATION
   ========================================================= */

async function login(
    username,
    password
) {

    const response =
        await apiPost(
            "/auth/login",
            {
                username: username,
                password: password
            }
        );


    localStorage.setItem(
        "token",
        response.token
    );

    localStorage.setItem(
        "username",
        response.username
    );

    localStorage.setItem(
        "role",
        response.role
    );

    return response;
}


/* =========================================================
   LOGOUT
   ========================================================= */

function logout() {

    localStorage.removeItem(
        "token"
    );

    localStorage.removeItem(
        "username"
    );

    localStorage.removeItem(
        "role"
    );

    window.location.href =
        "/login";
}


/* =========================================================
   AUTH CHECK
   ========================================================= */

function isLoggedIn() {

    return !!localStorage.getItem(
        "token"
    );
}


/* =========================================================
   CURRENT USER
   ========================================================= */

function getCurrentUsername() {

    return localStorage.getItem(
        "username"
    );
}

function getCurrentRole() {

    return localStorage.getItem(
        "role"
    );
}


/* =========================================================
   ROLE HELPERS
   ========================================================= */

function normalizeRole(role) {

    return String(role || "")
        .replace("ROLE_", "")
        .toUpperCase();
}


function displayRoleName(role) {

    const value =
        normalizeRole(role);

    if (value === "ADMIN") {
        return "Administrator";
    }

    if (value === "DOCTOR") {
        return "Doctor";
    }

    if (value === "PATIENT") {
        return "Patient";
    }

    return "User";
}


/* =========================================================
   PROFILE UI
   ========================================================= */

function setProfileUI(
    name,
    role
) {

    const safeName =
        name ||
        getCurrentUsername() ||
        "User";

    const normalizedRole =
        normalizeRole(
            role || getCurrentRole()
        );

    const roleText =
        displayRoleName(
            normalizedRole
        );


    const nameEl =
        document.getElementById(
            "profileName"
        );

    const roleEl =
        document.getElementById(
            "profileRole"
        );

    const avatarEl =
        document.getElementById(
            "profileAvatar"
        );

    const welcomeEl =
        document.getElementById(
            "welcomeName"
        );


    if (nameEl) {
        nameEl.textContent =
            safeName;
    }

    if (roleEl) {
        roleEl.textContent =
            roleText;
    }

    if (avatarEl) {
        avatarEl.textContent =
            safeName
                .charAt(0)
                .toUpperCase();
    }

    if (welcomeEl) {
        welcomeEl.textContent =
            safeName;
    }

    configurePatientProfileLink(
        normalizedRole
    );
}


/* =========================================================
   PATIENT PROFILE CLICK
   ========================================================= */

function configurePatientProfileLink(
    role
) {

    const normalizedRole =
        normalizeRole(
            role || getCurrentRole()
        );

    const profiles =
        document.querySelectorAll(
            ".profile"
        );


    profiles.forEach(profile => {

        if (
            normalizedRole ===
            "PATIENT"
        ) {

            profile.style.cursor =
                "pointer";

            profile.setAttribute(
                "role",
                "button"
            );

            profile.setAttribute(
                "tabindex",
                "0"
            );

            profile.setAttribute(
                "title",
                "View my patient details"
            );


            if (
                profile.dataset
                    .patientProfileBound !==
                "true"
            ) {

                const toggleProfile =
                    async (event) => {

                        event.preventDefault();
                        event.stopPropagation();

                        await togglePatientProfilePanel();
                    };


                profile.addEventListener(
                    "click",
                    toggleProfile
                );


                profile.addEventListener(
                    "keydown",
                    async event => {

                        if (
                            event.key ===
                            "Enter" ||
                            event.key ===
                            " "
                        ) {

                            event.preventDefault();
                            event.stopPropagation();

                            await togglePatientProfilePanel();
                        }
                    }
                );


                profile.dataset
                    .patientProfileBound =
                    "true";
            }

        } else {

            profile.style.cursor = "";

            profile.removeAttribute(
                "role"
            );

            profile.removeAttribute(
                "tabindex"
            );

            profile.removeAttribute(
                "title"
            );
        }
    });
}


/* =========================================================
   PATIENT PROFILE PANEL
   ========================================================= */

function ensurePatientProfilePanel() {

    if (
        document.getElementById(
            "patientProfilePanel"
        )
    ) {
        return;
    }


    const style =
        document.createElement(
            "style"
        );

    style.id =
        "patient-profile-panel-style";


    style.textContent = `

        .patient-profile-panel {
            position: fixed;
            top: 78px;
            right: 22px;
            width: min(380px, calc(100vw - 30px));
            max-height: calc(100vh - 100px);
            overflow-y: auto;
            background: #fff;
            border: 1px solid #dbe4ee;
            border-radius: 14px;
            box-shadow: 0 16px 40px rgba(15, 23, 42, .18);
            z-index: 5000;
            padding: 20px;
            display: none;
        }

        .patient-profile-panel.open {
            display: block;
        }

        .patient-profile-panel .pp-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 12px;
            margin-bottom: 16px;
        }

        .patient-profile-panel .pp-title {
            font-size: 20px;
            font-weight: 700;
            color: #102a43;
        }

        .patient-profile-panel .pp-subtitle {
            font-size: 12px;
            color: #627d98;
            margin-top: 2px;
        }

        .patient-profile-panel .pp-avatar {
            width: 48px;
            height: 48px;
            border-radius: 50%;
            flex: 0 0 48px;
            display: flex;
            align-items: center;
            justify-content: center;
            background: #1677ff;
            color: #fff;
            font-weight: 700;
            font-size: 20px;
        }

        .patient-profile-panel .pp-identity {
            display: flex;
            align-items: center;
            gap: 12px;
        }

        .patient-profile-panel .pp-close {
            border: 0;
            background: #f1f5f9;
            width: 34px;
            height: 34px;
            border-radius: 50%;
            cursor: pointer;
            color: #334e68;
        }

        .patient-profile-panel .pp-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 10px;
        }

        .patient-profile-panel .pp-field {
            border: 1px solid #dbe4ee;
            border-radius: 9px;
            padding: 10px 12px;
            background: #f8fafc;
        }

        .patient-profile-panel .pp-field.full {
            grid-column: 1/-1;
        }

        .patient-profile-panel .pp-label {
            display: block;
            font-size: 11px;
            color: #627d98;
            margin-bottom: 4px;
        }

        .patient-profile-panel .pp-value {
            font-size: 14px;
            font-weight: 600;
            color: #102a43;
            word-break: break-word;
        }

        .patient-profile-panel input,
        .patient-profile-panel select,
        .patient-profile-panel textarea {
            width: 100%;
            box-sizing: border-box;
            border: 1px solid #cbd5e1;
            border-radius: 7px;
            padding: 8px 9px;
            font-size: 14px;
            background: #fff;
        }

        .patient-profile-panel textarea {
            resize: vertical;
            min-height: 70px;
        }

        .patient-profile-panel .pp-actions {
            display: flex;
            gap: 8px;
            justify-content: flex-end;
            margin-top: 16px;
        }

        .patient-profile-panel .pp-btn {
            border: 0;
            border-radius: 7px;
            padding: 9px 13px;
            cursor: pointer;
            font-weight: 600;
        }

        .patient-profile-panel .pp-btn.primary {
            background: #1677ff;
            color: #fff;
        }

        .patient-profile-panel .pp-btn.secondary {
            background: #e2e8f0;
            color: #334e68;
        }

        .patient-profile-panel .pp-btn:disabled {
            opacity: .6;
            cursor: not-allowed;
        }

        .patient-profile-panel .pp-message {
            margin-top: 10px;
            font-size: 13px;
            display: none;
        }

        .patient-profile-panel .pp-message.error {
            color: #b91c1c;
            display: block;
        }

        .patient-profile-panel .pp-message.success {
            color: #166534;
            display: block;
        }

        @media (max-width: 520px) {
            .patient-profile-panel {
                right: 10px;
                top: 68px;
            }
        }
    `;


    document.head.appendChild(
        style
    );


    const panel =
        document.createElement(
            "div"
        );

    panel.id =
        "patientProfilePanel";

    panel.className =
        "patient-profile-panel";


    panel.innerHTML = `

        <div class="pp-header">

            <div class="pp-identity">

                <div
                    class="pp-avatar"
                    id="ppAvatar">
                    P
                </div>

                <div>

                    <div class="pp-title">
                        My Profile
                    </div>

                    <div class="pp-subtitle">
                        Patient details
                    </div>

                </div>

            </div>


            <button
                type="button"
                class="pp-close"
                id="ppClose"
                aria-label="Close">

                <i class="fa-solid fa-xmark"></i>

            </button>

        </div>


        <div id="ppViewMode">

            <div class="pp-grid">

                <div class="pp-field">
                    <span class="pp-label">
                        Name
                    </span>
                    <span
                        class="pp-value"
                        id="ppName">
                        --
                    </span>
                </div>

                <div class="pp-field">
                    <span class="pp-label">
                        Age
                    </span>
                    <span
                        class="pp-value"
                        id="ppAge">
                        --
                    </span>
                </div>

                <div class="pp-field">
                    <span class="pp-label">
                        Gender
                    </span>
                    <span
                        class="pp-value"
                        id="ppGender">
                        --
                    </span>
                </div>

                <div class="pp-field">
                    <span class="pp-label">
                        Phone
                    </span>
                    <span
                        class="pp-value"
                        id="ppPhone">
                        --
                    </span>
                </div>

                <div class="pp-field">
                    <span class="pp-label">
                        Email
                    </span>
                    <span
                        class="pp-value"
                        id="ppEmail">
                        --
                    </span>
                </div>

                <div class="pp-field full">
                    <span class="pp-label">
                        Address
                    </span>
                    <span
                        class="pp-value"
                        id="ppAddress">
                        --
                    </span>
                </div>

            </div>


            <div class="pp-actions">

                <button
                    type="button"
                    class="pp-btn primary"
                    id="ppEdit">

                    <i class="fa-solid fa-pen"></i>
                    Edit Profile

                </button>

            </div>

        </div>


        <form
            id="ppEditMode"
            style="display:none">

            <div class="pp-grid">

                <div class="pp-field">

                    <span class="pp-label">
                        Name
                    </span>

                    <input
                        id="ppEditName"
                        required>

                </div>


                <div class="pp-field">

                    <span class="pp-label">
                        Age
                    </span>

                    <input
                        id="ppEditAge"
                        type="number"
                        min="1"
                        required>

                </div>


                <div class="pp-field">

                    <span class="pp-label">
                        Gender
                    </span>

                    <select id="ppEditGender">

                        <option value="">
                            Select
                        </option>

                        <option>
                            Male
                        </option>

                        <option>
                            Female
                        </option>

                        <option>
                            Other
                        </option>

                    </select>

                </div>


                <div class="pp-field">

                    <span class="pp-label">
                        Phone
                    </span>

                    <input
                        id="ppEditPhone">

                </div>


                <div class="pp-field">

                    <span class="pp-label">
                        Email
                    </span>

                    <input
                        id="ppEditEmail"
                        type="email">

                </div>


                <div class="pp-field full">

                    <span class="pp-label">
                        Address
                    </span>

                    <textarea
                        id="ppEditAddress">
                    </textarea>

                </div>

            </div>


            <div
                class="pp-message"
                id="ppMessage">
            </div>


            <div class="pp-actions">

                <button
                    type="button"
                    class="pp-btn secondary"
                    id="ppCancel">

                    Cancel

                </button>


                <button
                    type="submit"
                    class="pp-btn primary"
                    id="ppSave">

                    Save Changes

                </button>

            </div>

        </form>
    `;


    document.body.appendChild(
        panel
    );


    document
        .getElementById("ppClose")
        .addEventListener(
            "click",
            () =>
                panel.classList.remove(
                    "open"
                )
        );


    document
        .getElementById("ppEdit")
        .addEventListener(
            "click",
            () => {

                document
                    .getElementById(
                        "ppViewMode"
                    )
                    .style.display =
                    "none";

                document
                    .getElementById(
                        "ppEditMode"
                    )
                    .style.display =
                    "block";

                populatePatientEditForm(
                    window.__currentPatientProfile ||
                    {}
                );
            }
        );


    document
        .getElementById("ppCancel")
        .addEventListener(
            "click",
            () => {

                document
                    .getElementById(
                        "ppEditMode"
                    )
                    .style.display =
                    "none";

                document
                    .getElementById(
                        "ppViewMode"
                    )
                    .style.display =
                    "block";

                clearProfileMessage();
            }
        );


    document
        .getElementById("ppEditMode")
        .addEventListener(
            "submit",
            savePatientProfileFromPanel
        );


    document.addEventListener(
        "click",
        event => {

            const currentPanel =
                document.getElementById(
                    "patientProfilePanel"
                );

            if (
                !currentPanel ||
                !currentPanel.classList.contains(
                    "open"
                )
            ) {
                return;
            }

            if (
                currentPanel.contains(
                    event.target
                )
            ) {
                return;
            }

            if (
                event.target.closest &&
                event.target.closest(
                    ".profile"
                )
            ) {
                return;
            }

            currentPanel.classList.remove(
                "open"
            );
        }
    );
}


/* =========================================================
   PATIENT PROFILE PANEL DATA
   ========================================================= */

function populatePatientProfilePanel(
    patient
) {

    window.__currentPatientProfile =
        patient || {};

    const p =
        patient || {};

    const name =
        p.name || "Patient";


    const set =
        (id, value) => {

            const el =
                document.getElementById(id);

            if (el) {

                el.textContent =
                    value ?? "--";
            }
        };


    set("ppName", name);
    set("ppAge", p.age);
    set("ppGender", p.gender);
    set("ppPhone", p.phone);
    set("ppEmail", p.email);
    set("ppAddress", p.address);


    const avatar =
        document.getElementById(
            "ppAvatar"
        );

    if (avatar) {

        avatar.textContent =
            name.charAt(0)
                .toUpperCase();
    }
}


function populatePatientEditForm(
    patient
) {

    const p =
        patient || {};


    document.getElementById(
        "ppEditName"
    ).value =
        p.name || "";


    document.getElementById(
        "ppEditAge"
    ).value =
        p.age ?? "";


    document.getElementById(
        "ppEditGender"
    ).value =
        p.gender || "";


    document.getElementById(
        "ppEditPhone"
    ).value =
        p.phone || "";


    document.getElementById(
        "ppEditEmail"
    ).value =
        p.email || "";


    document.getElementById(
        "ppEditAddress"
    ).value =
        p.address || "";
}


function clearProfileMessage() {

    const el =
        document.getElementById(
            "ppMessage"
        );

    if (el) {

        el.textContent = "";

        el.className =
            "pp-message";
    }
}


/* =========================================================
   TOGGLE PATIENT PROFILE
   ========================================================= */

async function togglePatientProfilePanel() {

    ensurePatientProfilePanel();

    const panel =
        document.getElementById(
            "patientProfilePanel"
        );


    if (
        panel.classList.contains(
            "open"
        )
    ) {

        panel.classList.remove(
            "open"
        );

        return;
    }


    panel.classList.add(
        "open"
    );

    clearProfileMessage();


    document.getElementById(
        "ppEditMode"
    ).style.display =
        "none";


    document.getElementById(
        "ppViewMode"
    ).style.display =
        "block";


    try {

        const patient =
            await apiGet(
                "/patients/me"
            );

        if (!patient) {

            throw new Error(
                "Unable to load patient profile."
            );
        }

        populatePatientProfilePanel(
            patient
        );

    } catch (error) {

        const el =
            document.getElementById(
                "ppMessage"
            );

        if (el) {

            el.textContent =
                error.message ||
                "Unable to load patient profile.";

            el.className =
                "pp-message error";
        }
    }
}


/* =========================================================
   SAVE PATIENT PROFILE
   ========================================================= */

async function savePatientProfileFromPanel(
    event
) {

    event.preventDefault();

    clearProfileMessage();


    const saveButton =
        document.getElementById(
            "ppSave"
        );


    saveButton.disabled =
        true;

    saveButton.textContent =
        "Saving...";


    try {

        const payload = {

            name:
                document
                    .getElementById(
                        "ppEditName"
                    )
                    .value
                    .trim(),

            age:
                Number(
                    document
                        .getElementById(
                            "ppEditAge"
                        )
                        .value
                ),

            gender:
                document
                    .getElementById(
                        "ppEditGender"
                    )
                    .value,

            phone:
                document
                    .getElementById(
                        "ppEditPhone"
                    )
                    .value
                    .trim(),

            email:
                document
                    .getElementById(
                        "ppEditEmail"
                    )
                    .value
                    .trim(),

            address:
                document
                    .getElementById(
                        "ppEditAddress"
                    )
                    .value
                    .trim()
        };


        if (!payload.name) {

            throw new Error(
                "Name is required."
            );
        }


        if (
            !payload.age ||
            payload.age < 1
        ) {

            throw new Error(
                "Enter a valid age."
            );
        }


        const updated =
            await apiPut(
                "/patients/me",
                payload
            );


        populatePatientProfilePanel(
            updated
        );


        setProfileUI(
            updated.name,
            "PATIENT"
        );


        document.getElementById(
            "ppEditMode"
        ).style.display =
            "none";


        document.getElementById(
            "ppViewMode"
        ).style.display =
            "block";


        const msg =
            document.getElementById(
                "ppMessage"
            );


        msg.textContent =
            "Profile updated successfully.";


        msg.className =
            "pp-message success";


    } catch (error) {

        const msg =
            document.getElementById(
                "ppMessage"
            );


        msg.textContent =
            error.message ||
            "Unable to update profile.";


        msg.className =
            "pp-message error";

    } finally {

        saveButton.disabled =
            false;

        saveButton.textContent =
            "Save Changes";
    }
}


/* =========================================================
   LOAD CENTRAL PROFILE
   ========================================================= */

async function loadCentralProfile() {

    const role =
        normalizeRole(
            getCurrentRole()
        );


    if (!role) {
        return;
    }


    // =====================================================
    // ADMIN
    // =====================================================

    if (role === "ADMIN") {

        setProfileUI(
            "Administrator",
            role
        );

        return;
    }


    // =====================================================
    // DOCTOR
    // =====================================================

    if (role === "DOCTOR") {

        try {

            const doctor =
                await apiGet(
                    "/doctors/me"
                );


            if (
                doctor &&
                doctor.name
            ) {

                setProfileUI(
                    doctor.name,
                    role
                );

                return;
            }

        } catch (error) {

            console.warn(
                "Unable to load doctor profile:",
                error
            );
        }


        setProfileUI(
            getCurrentUsername() ||
            "Doctor",
            role
        );

        return;
    }


    // =====================================================
    // PATIENT
    // =====================================================

    if (role === "PATIENT") {

        ensurePatientProfilePanel();

        try {

            const patient =
                await apiGet(
                    "/patients/me"
                );


            if (
                patient &&
                patient.name
            ) {

                setProfileUI(
                    patient.name,
                    role
                );

                return;
            }

        } catch (error) {

            console.warn(
                "Unable to load patient profile:",
                error
            );
        }


        setProfileUI(
            getCurrentUsername() ||
            "Patient",
            role
        );

        return;
    }
}


/* =========================================================
   ROLE BASED NAVIGATION
   ========================================================= */

function configureRoleNavigation() {

    const role =
        normalizeRole(
            getCurrentRole()
        );


    if (!role) {
        return;
    }


    const rules = {

        dashboard: [
            "ADMIN",
            "DOCTOR",
            "PATIENT"
        ],

        patients: [
            "ADMIN",
            "DOCTOR"
        ],

        doctors: [
            "ADMIN",
            "PATIENT"
        ],

        appointments: [
            "ADMIN",
            "DOCTOR",
            "PATIENT"
        ],

        medical: [
            "ADMIN",
            "DOCTOR",
            "PATIENT"
        ],

        billing: [
            "ADMIN",
            "PATIENT"
        ],

        reports: [
            "ADMIN"
        ],

        settings: [
            "ADMIN"
        ],

        staff: [
            "ADMIN"
        ],

        "patient-profile": [
            "PATIENT"
        ]
    };


    // =========================================================
    // REMOVE NOTIFICATIONS FROM SIDEBAR
    // =========================================================

    document
        .querySelectorAll(
            '.sidebar [data-nav-key="notifications"]'
        )
        .forEach(element => {

            element.remove();
        });


    // =========================================================
    // APPLY ROLE NAVIGATION
    // =========================================================

    document
        .querySelectorAll(
            "[data-nav-key]"
        )
        .forEach(link => {

            const key =
                link.dataset.navKey;


            // Notifications can NEVER
            // appear in the sidebar.

            if (
                key ===
                "notifications"
            ) {

                link.remove();

                return;
            }


            const allowed =
                rules[key] || [];


            link.style.display =
                allowed.includes(role)
                    ? "flex"
                    : "none";
        });


    // =========================================================
    // HIDE EMPTY SECTIONS
    // =========================================================

    document
        .querySelectorAll(
            ".sidebar .nav-section-title"
        )
        .forEach(section => {

            let next =
                section.nextElementSibling;

            let visible =
                false;


            while (
                next &&
                !next.classList.contains(
                    "nav-section-title"
                )
            ) {

                if (
                    next.matches &&
                    next.matches(
                        "a.nav-item"
                    ) &&
                    next.style.display !==
                        "none"
                ) {

                    visible =
                        true;

                    break;
                }


                next =
                    next.nextElementSibling;
            }


            section.style.display =
                visible
                    ? ""
                    : "none";
        });
}


/* =========================================================
   PAGE ACCESS
   ========================================================= */

function enforceRolePageAccess() {

    const role =
        normalizeRole(
            getCurrentRole()
        );


    const path =
        window.location.pathname;


    if (
        !role ||
        path === "/login" ||
        path === "/register" ||
        path === "/dashboard"
    ) {
        return;
    }


    const restrictions = {

        "/ui/patients": [
            "ADMIN",
            "DOCTOR"
        ],

        "/ui/doctors": [
            "ADMIN",
            "PATIENT"
        ],

        "/ui/appointments": [
            "ADMIN",
            "DOCTOR",
            "PATIENT"
        ],

        "/ui/medical-records": [
            "ADMIN",
            "DOCTOR",
            "PATIENT"
        ],

        "/ui/billing": [
            "ADMIN",
            "PATIENT"
        ],

        "/ui/reports": [
            "ADMIN"
        ],

        "/ui/notifications": [
            "ADMIN",
            "DOCTOR",
            "PATIENT"
        ],

        "/ui/settings": [
            "ADMIN"
        ],

        "/ui/staff": [
            "ADMIN"
        ],

        "/ui/patient-profile": [
            "PATIENT"
        ]
    };


    const allowed =
        restrictions[path];


    if (
        allowed &&
        !allowed.includes(role)
    ) {

        window.location.replace(
            "/dashboard"
        );
    }
}


/* =========================================================
   READ NOTIFICATION IDS
   ========================================================= */

function getReadNotificationIds() {

    const username =
        getCurrentUsername() ||
        "user";


    try {

        return new Set(
            JSON.parse(
                localStorage.getItem(
                    "readNotifications:" +
                    username
                ) || "[]"
            )
        );

    } catch (error) {

        return new Set();
    }
}


/* =========================================================
   NOTIFICATION BADGE
   ========================================================= */

async function refreshNotificationBadge() {

    if (!isLoggedIn()) {
        return;
    }


    try {

        const notifications =
            await apiGet(
                "/notifications"
            );


        const readIds =
            getReadNotificationIds();


        const unreadCount =
            Array.isArray(
                notifications
            )
                ? notifications.filter(
                    item =>
                        !readIds.has(
                            item.id
                        )
                ).length
                : 0;


        // =====================================================
        // HEADER BELL ONLY
        // =====================================================

        document
            .querySelectorAll(
                ".notification-count"
            )
            .forEach(element => {

                element.textContent =
                    unreadCount > 99
                        ? "99+"
                        : String(
                            unreadCount
                        );
            });


    } catch (error) {

        console.warn(
            "Unable to refresh notification badge:",
            error
        );
    }
}


/* =========================================================
   NOTIFICATION BELL
   ========================================================= */

function initializeNotificationBell() {

    document
        .querySelectorAll(
            ".header-icon.notification"
        )
        .forEach(button => {

            if (
                button.dataset
                    .notificationBound ===
                "true"
            ) {

                return;
            }


            button.dataset
                .notificationBound =
                "true";


            button.addEventListener(
                "click",
                function () {

                    window.location.href =
                        "/ui/notifications";
                }
            );
        });
}


/* =========================================================
   CENTRAL INITIALIZATION
   ========================================================= */

function initializeCentralRoleUI() {

    if (!isLoggedIn()) {
        return;
    }


    enforceRolePageAccess();

    configureRoleNavigation();

    initializeNotificationBell();

    refreshNotificationBadge();

    loadCentralProfile();
}


/* =========================================================
   PAGE LOAD
   ========================================================= */

document.addEventListener(
    "DOMContentLoaded",
    initializeCentralRoleUI
);


window.addEventListener(
    "load",
    function () {

        initializeCentralRoleUI();
    }
);