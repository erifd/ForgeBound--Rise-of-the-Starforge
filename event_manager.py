subscribers = {}

def subscribe(event_name, callback):
    if event_name not in subscribers:
        subscribers[event_name] = []
    subscribers[event_name].append(callback)

def broadcast(event_name, *args, **kwargs):
    if event_name in subscribers:
        for callback in subscribers[event_name]:
            callback(*args, **kwargs)
