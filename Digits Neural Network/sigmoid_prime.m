function [ out ] = sigmoid_prime( z )
    out = sigmoid(z).*(1-sigmoid(z));
end
