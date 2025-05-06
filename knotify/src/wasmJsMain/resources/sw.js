self.addEventListener('notificationclick', event => {
    event.notification.close(); // ferme la notification

    // Envoie l'action à toutes les pages clientes ouvertes
    event.waitUntil(
        self.clients.matchAll({type: 'window', includeUncontrolled: true})
            .then(clients =>
                clients.forEach(client => client.postMessage({
                    action: event.action || 'default'
                }))
            )
    );
});