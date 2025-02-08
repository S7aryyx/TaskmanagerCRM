document.addEventListener("DOMContentLoaded", async function () {
    const token = localStorage.getItem('jwt');

    if (!token) {
        showAlert("Токен не найден. Перенаправляем на страницу входа...", "error");
        setTimeout(() => window.location.href = '/login.html', 2000);
        return;
    }

    try {
        const response = await fetch('/api/auth/validate-token', {
            method: 'GET',
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!response.ok) {
            showAlert("Ошибка авторизации. Перенаправляем на страницу входа...", "error");
            setTimeout(() => window.location.href = '/login.html', 2000);
            return;
        }
        const data = await response.json();
        if (data.role !== 'ROLE_ADMIN') {
            showAlert("Недостаточно прав. Перенаправляем на страницу задач...", "error");
            setTimeout(() => window.location.href = '/tasks.html', 2000);
        }
    } catch (error) {
        showAlert("Ошибка проверки авторизации", "error");
        console.error(error);
    }
});

// Функция отображения уведомлений
function showAlert(message, type = "success") {
    const alertBox = document.getElementById("alertBox");
    alertBox.innerText = message;
    alertBox.className = `alert ${type}`;
    alertBox.style.display = "block";
    setTimeout(() => { alertBox.style.display = "none"; }, 3000);
}

// Загрузка задач для конкретного пользователя (админский эндпоинт)
async function loadTasks() {
    const username = document.getElementById('username').value.trim();
    const token = localStorage.getItem('jwt');

    if (!username) {
        showAlert("Введите имя пользователя!", "error");
        return;
    }

    try {
        const response = await fetch(`/api/tasks/admin/${encodeURIComponent(username)}`, {
            method: 'GET',
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!response.ok) {
            showAlert("Ошибка загрузки задач", "error");
            return;
        }
        const tasks = await response.json();
        renderTasks(tasks);
        displayStatusCounts(tasks);
    } catch (error) {
        showAlert("Не удалось загрузить задачи!", "error");
        console.error("Ошибка загрузки задач:", error);
    }
}

// Загрузка всех задач всех пользователей (админский эндпоинт)
async function loadAllTasks() {
    const token = localStorage.getItem('jwt');
    try {
        const response = await fetch(`/api/tasks/admin/all`, {
            method: 'GET',
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!response.ok) {
            showAlert("Ошибка загрузки всех задач", "error");
            return;
        }
        const tasks = await response.json();
        renderTasks(tasks);
        displayStatusCounts(tasks);
    } catch (error) {
        showAlert("Не удалось загрузить все задачи!", "error");
        console.error("Ошибка загрузки всех задач:", error);
    }
}

// Функция рендеринга списка задач в таблицу
function renderTasks(tasks) {
    const taskTable = document.getElementById('taskTable');
    taskTable.innerHTML = '';

    if (tasks.length === 0) {
        taskTable.innerHTML = "<tr><td colspan='6'>Задачи не найдены.</td></tr>";
        return;
    }

    tasks.forEach(task => {
        const row = document.createElement('tr');
        const owner = task.user && task.user.username ? task.user.username : "";
        const formattedDeadline = task.deadline ? task.deadline.replace("T", " ") : "";
        row.innerHTML = `
            <td>${task.title}</td>
            <td>${task.description}</td>
            <td>${task.status}</td>
            <td>${formattedDeadline}</td>
            <td>${owner}</td>
            <td>
                <button onclick="openEditForm(${task.id}, '${task.title}', '${task.description}', '${task.status}', '${task.deadline}')">✏️</button>
                <button onclick="deleteTask(${task.id})">🗑️</button>
            </td>
        `;
        taskTable.appendChild(row);
    });
}

// Функция подсчёта задач по статусам и отображения в блоке "statusCounts"
function displayStatusCounts(tasks) {
    const counts = {};
    tasks.forEach(task => {
        const status = task.status || "Неизвестно";
        counts[status] = (counts[status] || 0) + 1;
    });
    let output = "Подсчет задач по статусам: ";
    for (const status in counts) {
        output += `${status}: ${counts[status]}; `;
    }
    document.getElementById("statusCounts").innerText = output;
}

// Открытие модального окна для редактирования задачи
function openEditForm(id, title, description, status, deadline) {
    document.getElementById("editTaskId").value = id;
    document.getElementById("editTitle").value = title;
    document.getElementById("editDescription").value = description;
    document.getElementById("editStatus").value = status;
    document.getElementById("editDeadline").value = deadline;
    document.getElementById("editTaskModal").style.display = "block";
}

// Закрытие модального окна редактирования
function closeEditForm() {
    document.getElementById("editTaskModal").style.display = "none";
}

// Обновление задачи (админский эндпоинт)
async function updateTask() {
    const taskId = document.getElementById("editTaskId").value;
    const taskData = {
        title: document.getElementById("editTitle").value,
        description: document.getElementById("editDescription").value,
        status: document.getElementById("editStatus").value,
        deadline: document.getElementById("editDeadline").value
    };

    try {
        const response = await fetch(`/api/tasks/admin/${taskId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('jwt')}`
            },
            body: JSON.stringify(taskData)
        });
        if (response.ok) {
            showAlert("Задача обновлена!");
            closeEditForm();
            loadTasks();
        } else {
            const error = await response.text();
            showAlert(`Ошибка обновления задачи: ${error}`, "error");
        }
    } catch (error) {
        showAlert("Ошибка при обновлении задачи!", "error");
        console.error("Ошибка при обновлении задачи:", error);
    }
}

// Удаление задачи (админский эндпоинт)
async function deleteTask(taskId) {
    if (!confirm("Вы уверены, что хотите удалить задачу?")) return;
    try {
        const response = await fetch(`/api/tasks/admin/${taskId}`, {
            method: 'DELETE',
            headers: { 'Authorization': `Bearer ${localStorage.getItem('jwt')}` }
        });
        if (response.ok) {
            showAlert("Задача удалена!");
            loadTasks();
        } else {
            const error = await response.text();
            showAlert(`Ошибка удаления задачи: ${error}`, "error");
        }
    } catch (error) {
        showAlert("Ошибка при удалении задачи!", "error");
        console.error("Ошибка при удалении задачи:", error);
    }
}

// Добавление новой задачи (админский эндпоинт)
// Используется параметр запроса "username" для указания, для какого пользователя создается задача
async function addTask() {
    const targetUsername = document.getElementById("username").value.trim();
    const title = document.getElementById("newTitle").value;
    const description = document.getElementById("newDescription").value;
    const status = document.getElementById("newStatus").value;
    const deadline = document.getElementById("newDeadline").value;

    if (!targetUsername || !title || !description || !status || !deadline) {
        showAlert("Пожалуйста, заполните все поля!", "error");
        return;
    }

    const taskData = {
        title: title,
        description: description,
        status: status,
        deadline: deadline
    };

    try {
        const response = await fetch(`/api/tasks/admin?username=${encodeURIComponent(targetUsername)}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('jwt')}`
            },
            body: JSON.stringify(taskData)
        });
        if (response.ok) {
            showAlert("Задача добавлена!");
            loadTasks();
        } else {
            const error = await response.text();
            showAlert(`Ошибка добавления задачи: ${error}`, "error");
        }
    } catch (error) {
        showAlert("Ошибка при добавлении задачи!", "error");
        console.error("Ошибка при добавлении задачи:", error);
    }
}
