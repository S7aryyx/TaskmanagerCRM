const token = localStorage.getItem('jwt');
if (!token) {
    alert("Пожалуйста, авторизуйтесь.");
    window.location.href = '/login.html';  // Перенаправляем на страницу входа
}

let currentStatusFilter = ''; // Переменная для хранения текущего фильтра

// Функция для добавления новой задачи
document.getElementById('task-form').addEventListener('submit', async function (e) {
    e.preventDefault();

    const title = document.getElementById('title').value;
    const description = document.getElementById('description').value;
    const deadline = document.getElementById('deadline').value;

    try {
        const response = await fetch('/api/tasks', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({ title, description, deadline })
        });

        const data = await response.json();
        if (response.ok) {
            alert('Задача успешно добавлена');
            loadTasks();  // Загружаем задачи, чтобы отобразить добавленную
        } else {
            alert(data.message || 'Ошибка при добавлении задачи');
        }
    } catch (error) {
        alert('Ошибка при добавлении задачи');
        console.error(error);
    }
});

// Экспорт задач в CSV или PDF
document.getElementById('export-csv').addEventListener('click', () => exportTasks('csv'));
document.getElementById('export-pdf').addEventListener('click', () => exportTasks('pdf'));

async function exportTasks(format) {
    const statusFilter = document.getElementById('status-filter').value; // Получаем текущий фильтр

    try {
        const response = await fetch(`/api/tasks/export?format=${format}&status=${statusFilter}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.ok) {
            const blob = await response.blob();
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `tasks.${format}`;
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
        } else {
            alert('Ошибка экспорта задач.');
        }
    } catch (error) {
        alert('Не удалось экспортировать задачи.');
        console.error('Ошибка при экспорте задач:', error);
    }
}

// Функция для загрузки задач
async function loadTasks(statusFilter = '') {
    currentStatusFilter = statusFilter; // Обновляем текущий фильтр

    let url = '/api/tasks';
    if (statusFilter) {
        url += `?status=${statusFilter}`;
    }

    try {
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        const tasks = await response.json();
        const taskList = document.getElementById('taskList');
        taskList.innerHTML = '';  // Очищаем список перед добавлением новых задач

        if (tasks.length === 0) {
            taskList.innerHTML = "<p>Нет задач для отображения.</p>";
        } else {
            tasks.forEach(task => {
                const taskItem = document.createElement('div');
                taskItem.classList.add('task-item');
                taskItem.innerHTML = `
                    <h3>${task.title}</h3>
                    <p>${task.description}</p>
                    <p>Статус: ${task.status}</p>
                    <p>Срок: ${task.deadline}</p>
                    <button onclick="editTask(${task.id})">Редактировать</button>
                    <button onclick="deleteTask(${task.id})">Удалить</button>
                `;
                taskList.appendChild(taskItem);
            });

            // Обновляем счётчик задач
            document.getElementById('task-count').textContent = `Задачи: ${tasks.length}`;
        }
    } catch (error) {
        alert('Не удалось загрузить задачи.');
        console.error('Ошибка при загрузке задач:', error);
    }
}

// Функция для редактирования задачи
async function editTask(id) {
    const taskTitle = document.getElementById('edit-title');
    const taskDescription = document.getElementById('edit-description');
    const taskDeadline = document.getElementById('edit-deadline');
    const taskStatus = document.getElementById('edit-status');

    try {
        const response = await fetch(`/api/tasks/${id}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        const task = await response.json();
        // Заполняем форму редактирования данными задачи
        taskTitle.value = task.title;
        taskDescription.value = task.description;
        taskDeadline.value = task.deadline;
        taskStatus.value = task.status;

        // Показываем форму редактирования
        document.getElementById('add-task').style.display = 'none';
        document.getElementById('edit-task').style.display = 'block';

        // Обработчик для отправки обновленных данных
        document.getElementById('edit-task-form').addEventListener('submit', async function (e) {
            e.preventDefault();

            const updatedTask = {
                title: taskTitle.value,
                description: taskDescription.value,
                deadline: taskDeadline.value,
                status: taskStatus.value
            };

            try {
                const response = await fetch(`/api/tasks/${id}`, {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${token}`
                    },
                    body: JSON.stringify(updatedTask)
                });

                const updatedTaskData = await response.json();
                alert('Задача успешно обновлена');
                loadTasks();  // Перезагружаем задачи, чтобы отобразить обновленную задачу
                document.getElementById('add-task').style.display = 'block';
                document.getElementById('edit-task').style.display = 'none';
            } catch (error) {
                alert('Ошибка при обновлении задачи');
                console.error(error);
            }
        });
    } catch (error) {
        alert('Ошибка при получении данных задачи');
        console.error('Ошибка при получении данных задачи:', error);
    }
}

// Функция для отмены редактирования задачи
document.getElementById('cancel-edit').addEventListener('click', function () {
    document.getElementById('add-task').style.display = 'block';
    document.getElementById('edit-task').style.display = 'none';
});

// Функция для удаления задачи
async function deleteTask(id) {
    try {
        const response = await fetch(`/api/tasks/${id}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        // Проверяем, есть ли тело ответа (сервер может вернуть 204 No Content)
        let data = null;
        if (response.headers.get("Content-Type")?.includes("application/json")) {
            data = await response.json();
        }

        if (response.ok) {
            alert(data?.message || 'Задача удалена');  // Используем сообщение, если оно есть
            loadTasks();  // Перезагружаем список задач после удаления
        } else {
            alert(data?.message || 'Ошибка при удалении задачи');
        }
    } catch (error) {
        alert('Ошибка при удалении задачи');
        console.error(error);
    }
}


// Функция для фильтрации задач по статусу
document.getElementById('status-filter').addEventListener('change', function (e) {
    const status = e.target.value;
    loadTasks(status);  // Загружаем задачи с новым фильтром
});

// Загрузка задач при загрузке страницы (без фильтрации)
document.addEventListener('DOMContentLoaded', function () {
    loadTasks();  // Загружаем все задачи без фильтра по статусу
});
