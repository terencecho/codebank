%% Terence Cho
% SID: 23822733
% CS 189
% hw #6
% Utilizing Neural Networks to classify hand-written digits
format compact; clear; clc;

%%
% load training data
load('./digit-dataset/train.mat');

% data pre-processing
% re-shaping
reshaped_images = zeros(784,60000);
for i = 1:size(train_images,3)
    reshaped_images(:,i) = reshape(train_images(:,:,i),784,1);
end
train_images = reshaped_images;

% normalizing
train_images = train_images./255;

% randomize order of data
newRowOrder = randperm(60000);
random_images = train_images(:,newRowOrder);
random_labels = train_labels(newRowOrder);

% training set without validation set
train_images = random_images(:,1:55000);
train_labels = random_labels(1:55000);

% validation set
validation_images = random_images(:,55001:60000);
validation_labels = random_labels(55001:60000);

% overall training set
kaggle_training_images = random_images;
kaggle_training_labels = random_labels;

%% Training

% params
learning_rate = .055;

%
num_features = 28*28;
input_layer_size = num_features;
hidden_layer_size = 200;
output_layer_size = 10;

% init randomize weights
% Weights should be in a cell
W1 = .1 * randn(input_layer_size + 1,hidden_layer_size);
W2 = .1 * randn(hidden_layer_size + 1,output_layer_size);
%%
iteration = 0;
max_iterations = 200000;
save_point = 10000;
J = 0;
saved_iter = 0;

%%
tic
while iteration < max_iterations
    weights = {W1,W2};
    
    % pick random sample
    i = mod(iteration-1,numel(train_labels)) + 1;
    x = train_images(:,i)';  % sample image
    y = train_labels(i);     % sample label
    %x = kaggle_training_images(:,i)';  % sample image
    %y = kaggle_training_labels(i);     % sample label
    temp = zeros(1,10);
    temp(y+1) = 1;
    y = temp;
    
    % cost function
    % J = 0.5*sum((y-yHat).^2);
    
    % forward propegation => sigmoid([tanh([x 1] * W1) 1] * W2)
    z2 = [x 1] * W1;
    a2 = tanh(z2);
    z3 = [a2 1] * W2;
    yHat = sigmoid(z3);
    
    % backward pass
    % using mean square loss
    delta3 = -(y-yHat) .* sigmoid_prime(z3);
    dJdW2 = [a2 1]' * delta3;
    delta2 = delta3 *  W2(1:end-1,:)' .* tanh_prime(z2);
    dJdW1 = [x 1]' * delta2;
    
    % using cross-entropy loss
    %{
    dJdW2 = -[a2 1]' * (y-yHat);
    d1 = (y-yHat)./(yHat.*(1-yHat));
    check = (yHat.*(1-yHat));
    for p = 1:numel(check)
        if check(p) == 0
            d1(p) = 0;
        end
    end
    delta = -d1 .* sigmoid_prime(z3);
    delta2 = delta * W2(1:end-1,:)' .* tanh_prime(z2);
    dJdW1 = [x 1]' * delta2;
    %}
    
    iteration  = iteration + 1;
    
    % gradient update
    W1 = W1 - (learning_rate .* dJdW1);% .* (1/sqrt(iteration)));
    W2 = W2 - (learning_rate .* dJdW2);% .* (1/sqrt(iteration)));
    
    % summation of loss functions for each iteration
    J = J + 0.5*sum((y-yHat).^2);
    
    % reshuffle data
    if mod(iteration, 60000) == 0
        newRowOrder = randperm(60000);
        x = kaggle_training_images(:,newRowOrder)';  % sample image
        y = kaggle_training_labels(newRowOrder);     % sample label
    end
    
    %saving plot data
    %{
    if mod(iteration,1000) == 0
        saved_iter = saved_iter + 1;
        num_samples = size(validation_images,2);
        predict_labels = zeros(1,num_samples);
        % forward propegate test samples
        for i = 1:num_samples
            sample = validation_images(:,i);
            sample = sample';
            
            z2 = [sample 1] * W1;
            a2 = tanh(z2);
            z3 = [a2 1] * W2;
            yHat = sigmoid(z3);
            
            [not_used, p] = max(yHat);
            
            predict_labels(i) = p-1;
        end
        error = 0;
        for i = 1:num_samples
            if predict_labels(i) ~= validation_labels(i)
                error = error + 1;
            end
        end
        error = error/num_samples;
        saved_error{saved_iter} = {error, iteration};
    end
    %}
    
    % save weights progress
    if mod(iteration,save_point) == 0
        iteration
        J = J/save_point
        %last_saved_time_elapsed = toc
        %save('weights.mat','weights','iteration','last_saved_time_elapsed','J')
        if J < .005
            break
        end
        J = 0;
    end
    
end

time_elapsed = toc
save('plot_stuff.mat','saved_error')
save('weights.mat','weights','iteration','time_elapsed')

%% Validation Set Test
% load weights
load('weights_kaggle_sub2.mat')

W1 = weights{1};
W2 = weights{2};

num_samples = size(validation_images,2);
predict_labels = zeros(1,num_samples);
% forward propegate test samples
for i = 1:num_samples
    sample = validation_images(:,i);
    sample = sample';
    
    z2 = [sample 1] * W1;
    a2 = tanh(z2);
    z3 = [a2 1] * W2;
    yHat = sigmoid(z3);
    
    [not_used, p] = max(yHat);
    
    predict_labels(i) = p-1;
end
error = 0;
for i = 1:num_samples
    if predict_labels(i) ~= validation_labels(i)
        error = error + 1;
    end
end
error = error/num_samples

