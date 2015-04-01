(function () {
	var app = angular.module('myapp', []);

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

	app.directive('myPanels', function () {
		return {
			restrict: 'E',
			templateUrl: 'panels.html',
			controller: function () {
				this.tab = 1;
				this.selectTab = function (tab) {
					this.tab = tab;
				};
				this.isSelected = function (checkTab) {
					//console.log('isSelected( ' + this.tab + ' / ' + checkTab + ' )');
					return this.tab === checkTab;
				};
			},
			controllerAs: 'pan'
		};
	});
})();
