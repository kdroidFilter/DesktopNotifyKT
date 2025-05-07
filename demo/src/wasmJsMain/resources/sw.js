self.addEventListener('notificationclick', event => {
    event.notification.close();

    event.waitUntil(
        self.clients.matchAll({type: 'window', includeUncontrolled: true})
            .then(clients =>
                clients.forEach(client => client.postMessage({
                    action: event.action || 'default'
                }))
            )
    );
});
