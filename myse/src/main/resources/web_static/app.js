(function () {
	var app = angular.module('myapp', ['tabs']);

	app.controller('MyappController', function () {
		this.products = gems;

		this.addHint = function (product) {
			product.hints.push(product.nextHint);
			product.nextHint = '';
		}
	});

	var gems = [{
			name: 'Louis',
			price: 1.30,
			ok: false,
			hide: false,
			hints: ['good cat!', 'gentle cat']
		},
		{
			name: 'Charles',
			price: 40,
			ok: true,
			hints: ['bad cat!', 'evil cat!']
		},
		{
			name: 'Alice',
			price: 0,
			ok: true,
			hide: true
		}
	];

	app.directive('productTop', function () {
		return {
			restrict: 'E', // E for element, A for attribute
			templateUrl: 'product-top.html'
		};
	});
	
})();
