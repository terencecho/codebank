function [ out ] = tanh_prime( z )

out = 1 - tanh(z).^2;

end

