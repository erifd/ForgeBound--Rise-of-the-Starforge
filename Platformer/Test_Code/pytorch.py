import torch
import torch.nn as nn
import torch.optim as optim
import random
import string

# Rule-based label generator
def is_valid_sequence(seq):
    lower_seq = seq.lower()
    return 0 if 'aa' in lower_seq else 1  # 0 = invalid, 1 = valid

# Dataset generation
def generate_sequence(length=10):
    letters = string.ascii_letters
    return ''.join(random.choice(letters) for _ in range(length))

def generate_dataset(n_samples=1000, max_len=10):
    data = []
    labels = []
    for _ in range(n_samples):
        seq = generate_sequence(random.randint(5, max_len))
        label = is_valid_sequence(seq)
        data.append(seq)
        labels.append(label)
    return data, labels

# Character-level encoding
all_chars = string.ascii_letters
char_to_idx = {ch: i+1 for i, ch in enumerate(all_chars)}  # reserve 0 for padding
vocab_size = len(char_to_idx) + 1

def encode_sequence(seq, max_len):
    encoded = [char_to_idx.get(ch, 0) for ch in seq]
    if len(encoded) < max_len:
        encoded += [0] * (max_len - len(encoded))
    return encoded[:max_len]

# Define the model
class SequenceClassifier(nn.Module):
    def __init__(self, vocab_size, embed_dim, hidden_dim):
        super(SequenceClassifier, self).__init__()
        self.embedding = nn.Embedding(vocab_size, embed_dim, padding_idx=0)
        self.lstm = nn.LSTM(embed_dim, hidden_dim, batch_first=True)
        self.fc = nn.Linear(hidden_dim, 1)
        self.sigmoid = nn.Sigmoid()
    
    def forward(self, x):
        embedded = self.embedding(x)
        _, (hidden, _) = self.lstm(embedded)
        out = self.fc(hidden[-1])
        return self.sigmoid(out).squeeze()

# Hyperparameters
embed_dim = 16
hidden_dim = 32
max_len = 12
epochs = 10
batch_size = 32
lr = 0.005

# Generate data
sequences, labels = generate_dataset(n_samples=2000, max_len=max_len)
X = torch.tensor([encode_sequence(seq, max_len) for seq in sequences], dtype=torch.long)
y = torch.tensor(labels, dtype=torch.float32)

# Model, loss, optimizer
model = SequenceClassifier(vocab_size, embed_dim, hidden_dim)
criterion = nn.BCELoss()
optimizer = optim.Adam(model.parameters(), lr=lr)

# Training loop
for epoch in range(epochs):
    permutation = torch.randperm(X.size(0))
    total_loss = 0
    for i in range(0, X.size(0), batch_size):
        indices = permutation[i:i+batch_size]
        batch_X, batch_y = X[indices], y[indices]
        
        optimizer.zero_grad()
        outputs = model(batch_X)
        loss = criterion(outputs, batch_y)
        loss.backward()
        optimizer.step()
        total_loss += loss.item()
    
    print(f"Epoch {epoch+1}/{epochs}, Loss: {total_loss:.4f}")

# Test the model
def predict(seq):
    model.eval()
    with torch.no_grad():
        encoded = torch.tensor([encode_sequence(seq, max_len)], dtype=torch.long)
        output = model(encoded)
        return "Valid" if output.item() > 0.5 else "Invalid"

# Example predictions
print(predict("aAbCdEfG"))  # Should be valid
print(predict("aAccDef"))   # Should be invalid
